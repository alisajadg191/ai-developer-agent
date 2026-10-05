package com.sajad.aideveloperagent;

import com.sajad.aideveloperagent.service.AiGateway;
import com.sajad.aideveloperagent.exception.AgentException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises the actual Spring AI client against an Ollama-protocol stub, not a real model. */
@SpringBootTest(properties = "agent.model.timeout-ms=5000")
class SpringAiGatewayTests {
    static final AtomicReference<String> kind = new AtomicReference<>("valid");
    static final AtomicInteger calls = new AtomicInteger();
    static final AtomicReference<String> body = new AtomicReference<>();
    static final HttpServer server = startServer();
    @Autowired AiGateway gateway;
    @DynamicPropertySource static void properties(DynamicPropertyRegistry properties) {
        properties.add("spring.ai.ollama.base-url", () -> "http://127.0.0.1:" + server.getAddress().getPort());
    }
    static HttpServer startServer() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/chat", exchange -> {
                calls.incrementAndGet();
                body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String message = switch (kind.get()) {
                    case "invalid" -> "{\"role\":\"assistant\",\"content\":\"not valid JSON\"}";
                    case "loop" -> "{\"role\":\"assistant\",\"content\":\"\",\"tool_calls\":[{\"function\":{\"name\":\"getServiceHealth\",\"arguments\":{\"serviceName\":\"payment-service\"}}}]}";
                    default -> "{\"role\":\"assistant\",\"content\":\"{\\\"suspectedCause\\\":\\\"Pool configuration may be related.\\\",\\\"recommendedChecks\\\":[\\\"Compare settings.\\\"]}\"}";
                };
                var data = ("{\"model\":\"llama3.2\",\"created_at\":\"2026-10-01T00:00:00Z\",\"message\":" + message + ",\"done\":true,\"done_reason\":\"stop\",\"prompt_eval_count\":10,\"eval_count\":10}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, data.length);
                exchange.getResponseBody().write(data); exchange.close();
            });
            server.start(); return server;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    @BeforeEach void reset() { calls.set(0); kind.set("valid"); }
    @AfterAll static void stop() { server.stop(0); }
    @Test void analysisUsesOneCallWithoutToolDefinitions() {
        var report = gateway.analyse("payment-service", List.of("Pool exhausted."));
        assertEquals("Pool configuration may be related.", report.suspectedCause());
        assertEquals(1, calls.get());
        assertFalse(body.get().contains("getServiceHealth"));
        assertTrue(body.get().contains("600"), "Output token budget must reach Ollama");
    }
    @Test void malformedOutputIsReportedAsFailure() {
        kind.set("invalid");
        var ex = assertThrows(AgentException.class, () -> gateway.analyse("payment-service", List.of("Pool exhausted.")));
        assertEquals("INVALID_MODEL_RESPONSE", ex.code());
        assertEquals(1, calls.get());
    }
    @Test void repeatedToolRequestsTerminateAtBudget() {
        kind.set("loop");
        var ex = assertThrows(AgentException.class, () -> gateway.chat("Check payment-service health"));
        assertEquals("TOOL_LIMIT", ex.code());
        assertEquals(2, calls.get());
    }
}

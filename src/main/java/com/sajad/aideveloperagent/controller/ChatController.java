package com.sajad.aideveloperagent.controller;

import com.sajad.aideveloperagent.model.IncidentReport;
import com.sajad.aideveloperagent.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final InvestigationService investigations;
    private final ServiceCatalog catalog;
    private final AiGateway ai;
    private final ModelGate gate;
    private final String model;
    public ChatController(InvestigationService investigations, ServiceCatalog catalog, AiGateway ai,
            ModelGate gate, @Value("${spring.ai.ollama.chat.options.model:llama3.2}") String model) {
        this.investigations = investigations; this.catalog = catalog; this.ai = ai; this.gate = gate; this.model = model;
    }
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "UP", "model", model, "modelBusy", gate.busy(), "demoData", true);
    }
    @GetMapping("/services") public List<ServiceCatalog.ServiceSummary> services() { return catalog.services(); }
    @GetMapping("/services/{name}/snapshot") public ServiceCatalog.Snapshot snapshot(@PathVariable String name) { return catalog.lookup(name); }
    public record InvestigationRequest(String serviceName, String mode) {}
    @PostMapping("/ai/investigate")
    public IncidentReport investigate(@RequestBody InvestigationRequest request) {
        return investigations.investigate(request.serviceName(), request.mode() == null ? "demo" : request.mode());
    }
    @GetMapping("/ai/investigate")
    public IncidentReport investigateGet(@RequestParam String serviceName, @RequestParam(defaultValue = "demo") String mode) {
        return investigations.investigate(serviceName, mode);
    }
    public record ChatRequest(String message) {}
    @PostMapping("/ai/chat") public Map<String, Object> chatPost(@RequestBody ChatRequest request) {
        return Map.of("answer", chat(request.message()), "demoData", true, "analysisSource", "OLLAMA");
    }
    @GetMapping("/ai/chat") public String chat(@RequestParam String message) {
        if (message == null || message.isBlank() || message.length() > 2000)
            throw new IllegalArgumentException("message must contain 1–2000 characters.");
        return ai.chat(message);
    }
}

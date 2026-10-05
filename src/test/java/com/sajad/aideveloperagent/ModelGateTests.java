package com.sajad.aideveloperagent;

import com.sajad.aideveloperagent.service.ModelGate;
import com.sajad.aideveloperagent.exception.AgentException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ModelGateTests {
    @Test void deadlineDoesNotOpenSlotWhileWorkerStillRuns() throws Exception {
        var gate = new ModelGate(80);
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try {
            var failure = assertThrows(AgentException.class, () -> gate.execute(() -> {
                started.countDown(); release.await(); return "finished";
            }));
            assertEquals(HttpStatus.GATEWAY_TIMEOUT, failure.status());
            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertTrue(gate.busy());
            var busy = assertThrows(AgentException.class, () -> gate.execute(() -> "must not start"));
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, busy.status());
        } finally { release.countDown(); gate.stop(); }
    }
    @Test void successfulAndFailedTasksReleaseAdmission() {
        var gate = new ModelGate(2000);
        try {
            assertEquals("ok", gate.execute(() -> "ok"));
            assertFalse(gate.busy());
            var failure = assertThrows(AgentException.class, () -> gate.execute(() -> { throw new IllegalStateException("secret internal detail"); }));
            assertEquals("MODEL_UNAVAILABLE", failure.code());
            assertFalse(failure.getMessage().contains("secret"));
            assertFalse(gate.busy());
        } finally { gate.stop(); }
    }
}

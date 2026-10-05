package com.sajad.aideveloperagent.service;

import com.sajad.aideveloperagent.exception.AgentException;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

/** One admitted model task, no waiting request queue, a deadline for callers. */
@Component
public class ModelGate {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore permit = new Semaphore(1);
    private final long timeoutMs;
    public ModelGate(@Value("${agent.model.timeout-ms:45000}") long timeoutMs) {
        this.timeoutMs = Math.max(1, timeoutMs);
    }
    public <T> T execute(Callable<T> task) {
        if (!permit.tryAcquire()) throw new AgentException(HttpStatus.TOO_MANY_REQUESTS,
            "MODEL_BUSY", "Another AI request is running. Wait for it to finish, or use demo mode.");
        Future<T> future;
        try {
            future = executor.submit(() -> { try { return task.call(); } finally { permit.release(); } });
        } catch (RuntimeException ex) { permit.release(); throw ex; }
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            // Do not release admission here: a transport may not respond to interruption.
            // Keep the slot occupied until the worker actually exits.
            throw new AgentException(HttpStatus.GATEWAY_TIMEOUT, "MODEL_TIMEOUT",
                "The model exceeded the response deadline. Use demo mode or check Ollama. A request may still be finishing.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AgentException(HttpStatus.SERVICE_UNAVAILABLE, "REQUEST_INTERRUPTED", "Request interrupted.");
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof AgentException ae) throw ae;
            throw new AgentException(HttpStatus.BAD_GATEWAY, "MODEL_UNAVAILABLE",
                "Ollama could not complete the request. Check that it is running and the configured model is downloaded.");
        }
    }
    public boolean busy() { return permit.availablePermits() == 0; }
    @PreDestroy public void stop() { executor.shutdownNow(); }
}

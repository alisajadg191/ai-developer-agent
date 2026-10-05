package com.sajad.aideveloperagent.service;

import com.sajad.aideveloperagent.model.ServiceHealth;
import org.springframework.stereotype.Component;
import java.util.*;

/** Fixed fictional fixtures. No production systems are accessed. */
@Component
public class ServiceCatalog {
    public record Deployment(String version, String time, String status, String change) {}
    public record Snapshot(ServiceHealth health, List<String> logs, Deployment deployment) {}
    public record ServiceSummary(String name, String description, String health) {}
    private final Map<String, Snapshot> data = Map.of(
        "payment-service", new Snapshot(new ServiceHealth("payment-service", "UNHEALTHY", true),
            List.of("10:05 ERROR Unable to acquire JDBC connection.",
                    "10:05 ERROR Database connection pool exhausted.",
                    "10:06 ERROR Payment request failed."),
            new Deployment("2.4.1", "10:00", "SUCCESS", "Updated database connection pool configuration.")),
        "order-service", new Snapshot(new ServiceHealth("order-service", "HEALTHY", true),
            List.of("10:05 INFO Order created successfully.", "10:06 INFO Health check passed."),
            new Deployment("1.8.0", "09:00", "SUCCESS", "Updated order validation.")),
        "inventory-service", new Snapshot(new ServiceHealth("inventory-service", "UNHEALTHY", true),
            List.of("10:05 WARN Kafka consumer lag: 4200 messages.",
                    "10:06 ERROR Inventory update processing exceeded timeout."),
            new Deployment("3.2.0", "08:30", "SUCCESS", "Updated inventory event processing."))
    );
    public String normalise(String name) {
        String value = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        if (!value.matches("[a-z0-9][a-z0-9-]{0,63}"))
            throw new IllegalArgumentException("serviceName must be 1–64 letters, numbers or hyphens, starting with a letter or number.");
        return value;
    }
    public Snapshot lookup(String name) {
        String key = normalise(name);
        return data.getOrDefault(key, new Snapshot(new ServiceHealth(key, "UNKNOWN", true), List.of(), null));
    }
    public List<ServiceSummary> services() {
        return List.of(new ServiceSummary("payment-service", "Database connection pool incident", "UNHEALTHY"),
            new ServiceSummary("order-service", "Healthy service baseline", "HEALTHY"),
            new ServiceSummary("inventory-service", "Kafka consumer lag incident", "UNHEALTHY"));
    }
}

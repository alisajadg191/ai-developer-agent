package com.sajad.aideveloperagent.tools;

import com.sajad.aideveloperagent.model.ServiceHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ServiceTools {

    private static final Logger log =
            LoggerFactory.getLogger(ServiceTools.class);

    @Tool(description = """
            Gets simulated service health.
            The health field is authoritative for this demo.
            Health does not indicate the underlying failure cause.
            """)
    public ServiceHealth getServiceHealth(
            @ToolParam(description = "Service name, such as payment-service")
            String serviceName) {

        log.info("TOOL CALLED: getServiceHealth({})", serviceName);

        String name = normalise(serviceName);

        String health = switch (name) {
            case "payment-service" -> "UNHEALTHY";
            case "order-service" -> "HEALTHY";
            default -> "UNKNOWN";
        };

        return new ServiceHealth(name, health, true);
    }

    @Tool(description = """
            Gets simulated recent application logs for a service.
            Use this to investigate errors or why a service is unhealthy.
            Returns demo data, not live logs.
            """)
    public String getRecentLogs(
            @ToolParam(description = "Service name, such as payment-service")
            String serviceName) {

        log.info("TOOL CALLED: getRecentLogs({})", serviceName);

        String name = normalise(serviceName);

        if (name.isEmpty()) {
            return "DEMO DATA: No service name provided.";
        }

        return switch (name) {
            case "payment-service" -> """
                    DEMO DATA — payment-service logs:
                    10:05 ERROR Unable to acquire JDBC connection.
                    10:05 ERROR Database connection pool exhausted.
                    10:06 ERROR Payment request failed.
                    """;

            case "order-service" -> """
                    DEMO DATA — order-service logs:
                    10:05 INFO Order created successfully.
                    10:06 INFO Health check passed.
                    """;

            default ->
                    "DEMO DATA: Service not found. No logs available.";
        };
    }

    @Tool(description = """
            Gets simulated information about a service's latest deployment.
            Use this to check whether a deployment happened near an incident.
            Deployment timing alone does not prove the cause.
            Returns demo data.
            """)
    public String getLastDeployment(
            @ToolParam(description = "Service name, such as payment-service")
            String serviceName) {

        log.info("TOOL CALLED: getLastDeployment({})", serviceName);

        String name = normalise(serviceName);

        if (name.isEmpty()) {
            return "DEMO DATA: No service name provided.";
        }

        return switch (name) {
            case "payment-service" -> """
                    DEMO DATA — same day as the sample logs:
                    Service: payment-service
                    Version: 2.4.1
                    Deployment time: 10:00
                    Deployment status: SUCCESS
                    Change: Updated database connection pool configuration.
                    """;

            case "order-service" -> """
                    DEMO DATA — same day as the sample logs:
                    Service: order-service
                    Version: 1.8.0
                    Deployment time: 09:00
                    Deployment status: SUCCESS
                    Change: Updated order validation.
                    """;

            default ->
                    "DEMO DATA: Service not found. No deployment data available.";
        };
    }

    private String normalise(String serviceName) {
        return serviceName == null
                ? ""
                : serviceName.trim().toLowerCase(Locale.ROOT);
    }
}

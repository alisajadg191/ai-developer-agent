package com.sajad.aideveloperagent.tools;

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
            Gets demo health information for a service.
            Use this when the user asks about service health or availability.
            This returns simulated data, not live monitoring data.
            """)
    public String getServiceHealth(
            @ToolParam(description = "Service name, such as payment-service")
            String serviceName) {

        log.info("TOOL CALLED: getServiceHealth({})", serviceName);

        if (serviceName == null || serviceName.isBlank()) {
            return "DEMO DATA: No service name provided.";
        }

        return switch (serviceName.trim().toLowerCase(Locale.ROOT)) {
            case "payment-service" ->
                    "DEMO DATA: payment-service is UNHEALTHY. Cause unknown.";

            case "order-service" ->
                    "DEMO DATA: order-service is HEALTHY.";

            default ->
                    "DEMO DATA: Service not found. Health is UNKNOWN.";
        };
    }
}

package com.sajad.aideveloperagent.tools;

import com.sajad.aideveloperagent.model.ServiceHealth;
import com.sajad.aideveloperagent.service.ServiceCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ServiceTools {
    private static final Logger log = LoggerFactory.getLogger(ServiceTools.class);
    private final ServiceCatalog catalog;
    public ServiceTools(ServiceCatalog catalog) { this.catalog = catalog; }

    @Tool(description = "Read simulated health for a service. UNKNOWN means no fixture exists. All data is fictional.")
    public ServiceHealth getServiceHealth(@ToolParam(description = "Service name, e.g. payment-service") String serviceName) {
        log.info("TOOL CALLED: getServiceHealth({})", serviceName);
        return catalog.lookup(serviceName).health();
    }
    public record LogResult(String service, List<String> logs, boolean demoData) {}
    @Tool(description = "Read simulated application logs for a service. An empty list means no logs are available.")
    public LogResult getRecentLogs(@ToolParam(description = "Service name") String serviceName) {
        log.info("TOOL CALLED: getRecentLogs({})", serviceName);
        var snapshot = catalog.lookup(serviceName);
        return new LogResult(snapshot.health().service(), snapshot.logs(), true);
    }
    public record DeploymentResult(String service, ServiceCatalog.Deployment deployment, boolean demoData) {}
    @Tool(description = "Read a simulated deployment. A null deployment means unavailable. Timing does not prove causation.")
    public DeploymentResult getLastDeployment(@ToolParam(description = "Service name") String serviceName) {
        log.info("TOOL CALLED: getLastDeployment({})", serviceName);
        var snapshot = catalog.lookup(serviceName);
        return new DeploymentResult(snapshot.health().service(), snapshot.deployment(), true);
    }
}

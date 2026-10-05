package com.sajad.aideveloperagent.service;

import com.sajad.aideveloperagent.model.*;
import com.sajad.aideveloperagent.model.IncidentReport.ToolTrace;
import com.sajad.aideveloperagent.tools.ServiceTools;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class InvestigationService {
    private final ServiceTools tools;
    private final ServiceCatalog catalog;
    private final AiGateway ai;
    public InvestigationService(ServiceTools tools, ServiceCatalog catalog, AiGateway ai) {
        this.tools = tools; this.catalog = catalog; this.ai = ai;
    }
    public IncidentReport investigate(String service, String mode) {
        long start = System.nanoTime();
        String name = catalog.normalise(service);
        if (!List.of("demo", "ai").contains(mode)) throw new IllegalArgumentException("mode must be demo or ai.");
        var health = tools.getServiceHealth(name);
        var logs = tools.getRecentLogs(name);
        var deployment = tools.getLastDeployment(name).deployment();
        var evidence = new ArrayList<String>();
        evidence.add("DEMO health: " + health.health());
        evidence.addAll(logs.logs());
        if (deployment != null) evidence.add("Same fixture day, " + deployment.time() + ": deployment "
            + deployment.version() + " " + deployment.status() + ". " + deployment.change());
        var trace = new ArrayList<ToolTrace>(List.of(
            new ToolTrace("getServiceHealth", "COMPLETE", health.health()),
            new ToolTrace("getRecentLogs", "COMPLETE", logs.logs().size() + " log entries"),
            new ToolTrace("getLastDeployment", "COMPLETE", deployment == null ? "No deployment available" : deployment.version())));
        var warnings = new ArrayList<String>();
        warnings.add("All service data is fictional. No production systems are accessed or modified.");
        Analysis analysis;
        String source;
        if (health.health().equals("UNKNOWN")) {
            analysis = new Analysis("Unknown — this service is not in the demo catalog.", List.of("Check the service name against the catalog."));
            source = "RULE_BASED";
        } else if (health.health().equals("HEALTHY")) {
            analysis = new Analysis("No failure observed in the supplied demo evidence.", List.of());
            source = "RULE_BASED";
        } else if (mode.equals("ai")) {
            analysis = ai.analyse(name, List.copyOf(evidence));
            source = "OLLAMA";
            warnings.add("AI suggestions are unverified. Compare them with the evidence before acting.");
        } else {
            analysis = name.equals("payment-service")
                ? new Analysis("Pool exhaustion is observed. The recent pool configuration change may be related; the underlying cause is unconfirmed.",
                    List.of("Compare pool configuration before and after deployment.", "Check database connection limits and active connections.", "Look for long-running queries or unreleased connections."))
                : new Analysis("Consumer lag and processing timeouts are observed. The reason for slow consumption is unconfirmed.",
                    List.of("Compare event arrival and consumer processing rates.", "Inspect consumer errors and downstream latency.", "Review the event-processing deployment changes."));
            source = "DEMO_TEMPLATE";
        }
        trace.add(new ToolTrace("analysis", "COMPLETE", source));
        String cause = source.equals("OLLAMA") ? "Unconfirmed hypothesis: " + analysis.suspectedCause() : analysis.suspectedCause();
        return new IncidentReport(UUID.randomUUID().toString(), Instant.now(), name, health.health(),
            List.copyOf(evidence), cause, List.copyOf(analysis.recommendedChecks()), true, mode, source,
            (System.nanoTime() - start) / 1_000_000, List.copyOf(warnings), List.copyOf(trace));
    }
}

package com.sajad.aideveloperagent.model;

import java.time.Instant;
import java.util.List;

public record IncidentReport(String id, Instant generatedAt, String service, String health,
        List<String> evidence, String suspectedCause, List<String> recommendedChecks,
        boolean demoData, String mode, String analysisSource, long durationMs,
        List<String> warnings, List<ToolTrace> trace) {
    public record ToolTrace(String tool, String status, String detail) {}
}

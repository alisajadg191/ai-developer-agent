package com.sajad.aideveloperagent.model;

import java.util.List;

public record IncidentReport(
        String service,
        String health,
        List<String> evidence,
        String suspectedCause,
        List<String> recommendedChecks,
        boolean demoData
) {}

package com.example.loganalyzer.model;

import java.util.List;

public record LogAnalysisResult(
    AnalysisType type,
    Severity severity,
    String summary,
    String rootCause,
    List<String> suggestions,
    List<String> patterns,
    int totalLogsAnalyzed,
    int errorCount,
    int warnCount
) {
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

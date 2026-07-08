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
    public LogAnalysisResult {
        suggestions = List.copyOf(suggestions);
        patterns = List.copyOf(patterns);
    }

    public List<String> suggestions() {
        return List.copyOf(suggestions);
    }

    public List<String> patterns() {
        return List.copyOf(patterns);
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

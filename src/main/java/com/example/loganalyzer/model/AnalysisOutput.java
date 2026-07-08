package com.example.loganalyzer.model;

import java.util.List;

public record AnalysisOutput(
    String summary,
    String rootCause,
    List<String> suggestions,
    List<String> patterns
) {
    public AnalysisOutput {
        suggestions = List.copyOf(suggestions);
        patterns = List.copyOf(patterns);
    }

    public List<String> suggestions() {
        return List.copyOf(suggestions);
    }

    public List<String> patterns() {
        return List.copyOf(patterns);
    }
}

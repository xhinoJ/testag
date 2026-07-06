package com.example.loganalyzer.model;

import java.util.List;

public record AnalysisOutput(
    String summary,
    String rootCause,
    List<String> suggestions,
    List<String> patterns
) {}

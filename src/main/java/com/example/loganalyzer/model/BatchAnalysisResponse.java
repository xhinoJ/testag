package com.example.loganalyzer.model;

import java.time.LocalDateTime;
import java.util.List;

public record BatchAnalysisResponse(
    String status,
    LocalDateTime analyzedAt,
    int totalResults,
    List<LogAnalysisResult> results
) {
    public static BatchAnalysisResponse success(List<LogAnalysisResult> results) {
        return new BatchAnalysisResponse("success", LocalDateTime.now(), results.size(), results);
    }
}

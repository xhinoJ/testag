package com.example.loganalyzer.model;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResponse(
    String status,
    LocalDateTime analyzedAt,
    LogAnalysisResult result
) {
    public static AnalysisResponse success(LogAnalysisResult result) {
        return new AnalysisResponse("success", LocalDateTime.now(), result);
    }
}

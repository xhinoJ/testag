package com.example.loganalyzer.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogAnalysisRequest(
    @NotBlank(message = "Log content must not be blank")
    @Size(max = 100_000, message = "Log content must not exceed 100,000 characters")
    String logContent,
    AnalysisType analysisType
) {
    public LogAnalysisRequest {
        if (analysisType == null) {
            analysisType = AnalysisType.FULL;
        }
    }
}

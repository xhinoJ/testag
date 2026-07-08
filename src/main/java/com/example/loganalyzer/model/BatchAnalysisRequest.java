package com.example.loganalyzer.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchAnalysisRequest(
    @NotEmpty(message = "Log contents must not be empty")
    @Size(min = 1, max = 50, message = "Batch must contain between 1 and 50 log entries")
    List<String> logContents,

    AnalysisType analysisType
) {
    public BatchAnalysisRequest {
        logContents = List.copyOf(logContents);
        if (analysisType == null) {
            analysisType = AnalysisType.FULL;
        }
    }

    public List<String> logContents() {
        return List.copyOf(logContents);
    }
}

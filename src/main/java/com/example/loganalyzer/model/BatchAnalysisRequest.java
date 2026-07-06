package com.example.loganalyzer.model;

import java.util.List;

public record BatchAnalysisRequest(
    List<String> logContents,
    AnalysisType analysisType
) {
    public BatchAnalysisRequest {
        if (analysisType == null) {
            analysisType = AnalysisType.FULL;
        }
        if (logContents == null) {
            logContents = List.of();
        }
    }
}

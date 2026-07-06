package com.example.loganalyzer.model;

public record LogAnalysisRequest(
    String logContent,
    AnalysisType analysisType
) {
    public LogAnalysisRequest {
        if (analysisType == null) {
            analysisType = AnalysisType.FULL;
        }
    }
}

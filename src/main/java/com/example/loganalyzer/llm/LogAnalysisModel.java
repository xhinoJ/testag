package com.example.loganalyzer.llm;

import com.example.loganalyzer.model.AnalysisOutput;

public interface LogAnalysisModel {

    AnalysisOutput analyze(String prompt);
}

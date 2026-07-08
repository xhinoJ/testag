package com.example.loganalyzer.service;

import com.example.loganalyzer.exception.LogAnalysisException;
import com.example.loganalyzer.llm.LogAnalysisModel;
import com.example.loganalyzer.model.AnalysisOutput;
import com.example.loganalyzer.model.AnalysisType;
import com.example.loganalyzer.model.LogAnalysisResult;
import com.example.loganalyzer.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);

    private final LogAnalysisModel logAnalysisModel;
    private final LogParserService logParserService;
    private final PromptTemplateService promptTemplateService;

    public LogAnalysisService(LogAnalysisModel logAnalysisModel,
                              LogParserService logParserService,
                              PromptTemplateService promptTemplateService) {
        this.logAnalysisModel = logAnalysisModel;
        this.logParserService = logParserService;
        this.promptTemplateService = promptTemplateService;
    }

    public List<LogEntry> parseLogs(String rawLogs) {
        return logParserService.parse(rawLogs);
    }

    public LogAnalysisResult analyze(List<LogEntry> logs, AnalysisType type) {
        log.info("Analyzing {} log entries with analysis type: {}", logs.size(), type);

        String prompt = switch (type) {
            case SUMMARY -> promptTemplateService.buildSummaryPrompt(logs);
            case ROOT_CAUSE -> {
                LogEntry errorLog = logs.stream()
                    .filter(e -> e.level() == LogEntry.LogLevel.ERROR ||
                                 e.level() == LogEntry.LogLevel.FATAL)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                        "No ERROR or FATAL log entries found for root cause analysis"));
                yield promptTemplateService.buildRootCausePrompt(errorLog);
            }
            case PATTERNS -> promptTemplateService.buildPatternsPrompt(logs);
            case FULL -> promptTemplateService.buildFullAnalysisPrompt(logs);
        };

        AnalysisOutput output;
        try {
            output = logAnalysisModel.analyze(prompt);
        } catch (Exception e) {
            log.error("AI analysis failed for type {}", type, e);
            throw new LogAnalysisException("AI service unavailable: " + e.getMessage(), e);
        }

        long errorCount = logs.stream()
            .filter(e -> e.level() == LogEntry.LogLevel.ERROR || e.level() == LogEntry.LogLevel.FATAL)
            .count();
        long warnCount = logs.stream()
            .filter(e -> e.level() == LogEntry.LogLevel.WARN)
            .count();

        LogAnalysisResult.Severity severity = determineSeverity(errorCount, warnCount, logs.size());

        return new LogAnalysisResult(
            type,
            severity,
            output.summary(),
            output.rootCause(),
            output.suggestions(),
            output.patterns(),
            logs.size(),
            (int) errorCount,
            (int) warnCount
        );
    }

    private LogAnalysisResult.Severity determineSeverity(long errorCount, long warnCount, int total) {
        if (total == 0) {
            return LogAnalysisResult.Severity.LOW;
        }
        if (errorCount == 0 && warnCount == 0) {
            return LogAnalysisResult.Severity.LOW;
        }
        double errorRatio = (double) errorCount / total;
        if (errorRatio > 0.3) {
            return LogAnalysisResult.Severity.CRITICAL;
        }
        if (errorRatio > 0.1) {
            return LogAnalysisResult.Severity.HIGH;
        }
        if (errorCount > 0) {
            return LogAnalysisResult.Severity.MEDIUM;
        }
        return LogAnalysisResult.Severity.LOW;
    }
}

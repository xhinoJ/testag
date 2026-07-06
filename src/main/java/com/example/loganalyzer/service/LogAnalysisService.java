package com.example.loganalyzer.service;

import com.example.loganalyzer.model.AnalysisType;
import com.example.loganalyzer.model.LogAnalysisResult;
import com.example.loganalyzer.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);

    private final ChatClient chatClient;
    private final LogParserService logParserService;
    private final PromptTemplateService promptTemplateService;

    public LogAnalysisService(ChatClient.Builder chatClientBuilder,
                              LogParserService logParserService,
                              PromptTemplateService promptTemplateService) {
        this.chatClient = chatClientBuilder.build();
        this.logParserService = logParserService;
        this.promptTemplateService = promptTemplateService;
    }

    public LogAnalysisResult analyzeRawLogs(String rawLogs) {
        List<LogEntry> entries = logParserService.parse(rawLogs);
        return analyze(entries, AnalysisType.FULL);
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
                    .orElse(logs.get(0));
                yield promptTemplateService.buildRootCausePrompt(errorLog);
            }
            case PATTERNS -> promptTemplateService.buildPatternsPrompt(logs);
            case FULL -> promptTemplateService.buildFullAnalysisPrompt(logs);
        };

        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        return parseResponse(response, type, logs);
    }

    private LogAnalysisResult parseResponse(String response, AnalysisType type, List<LogEntry> logs) {
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
            response,
            extractRootCause(response),
            extractSuggestions(response),
            extractPatterns(response),
            logs.size(),
            (int) errorCount,
            (int) warnCount
        );
    }

    private LogAnalysisResult.Severity determineSeverity(long errorCount, long warnCount, int total) {
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

    private String extractRootCause(String response) {
        int rootCauseIdx = response.indexOf("root cause");
        if (rootCauseIdx == -1) {
            rootCauseIdx = response.indexOf("Root Cause");
        }
        if (rootCauseIdx == -1) {
            rootCauseIdx = response.indexOf("ROOT CAUSE");
        }
        if (rootCauseIdx >= 0) {
            int end = response.indexOf("\n\n", rootCauseIdx);
            if (end == -1) end = Math.min(rootCauseIdx + 500, response.length());
            return response.substring(rootCauseIdx, end).trim();
        }
        return response.substring(0, Math.min(500, response.length()));
    }

    private List<String> extractSuggestions(String response) {
        return java.util.Arrays.stream(response.split("\n"))
            .filter(line -> line.contains("suggest") || line.contains("recommend") ||
                           line.contains("should") || line.contains("fix"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
    }

    private List<String> extractPatterns(String response) {
        return java.util.Arrays.stream(response.split("\n"))
            .filter(line -> line.contains("pattern") || line.contains("recurring") ||
                           line.contains("repeated") || line.contains("consistent"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
    }
}

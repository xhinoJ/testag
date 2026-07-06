package com.example.loganalyzer.service;

import com.example.loganalyzer.model.LogEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptTemplateService {

    public String buildSummaryPrompt(List<LogEntry> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Java/Spring Boot log analyst.\n\n");
        sb.append("Analyze the following log entries and provide a concise summary.\n");
        sb.append("Focus on: errors, warnings, performance issues, and notable patterns.\n\n");
        sb.append("LOGS:\n");
        for (LogEntry log : logs) {
            sb.append(String.format("[%s] %s %s - %s\n",
                log.timestamp(), log.level(), log.logger(), log.message()));
        }
        return sb.toString();
    }

    public String buildRootCausePrompt(LogEntry log) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a senior Java developer performing root cause analysis.\n\n");
        sb.append("Given this error log, identify:\n");
        sb.append("1. The root cause\n");
        sb.append("2. Contributing factors\n");
        sb.append("3. Suggested fixes with code examples\n\n");
        sb.append("ERROR LOG:\n");
        sb.append(String.format("[%s] %s %s - %s\n",
            log.timestamp(), log.level(), log.logger(), log.message()));
        if (log.stackTrace() != null && !log.stackTrace().isEmpty()) {
            sb.append("\nSTACK TRACE:\n").append(log.stackTrace());
        }
        return sb.toString();
    }

    public String buildPatternsPrompt(List<LogEntry> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert log pattern analyst.\n\n");
        sb.append("Analyze the following logs and identify recurring patterns:\n");
        sb.append("1. Error patterns (repeated exceptions)\n");
        sb.append("2. Performance patterns (slow operations)\n");
        sb.append("3. Timing patterns (issues at specific times)\n");
        sb.append("4. Sequence patterns (errors that follow a sequence)\n\n");
        sb.append("LOGS:\n");
        for (LogEntry log : logs) {
            sb.append(String.format("[%s] %s %s - %s\n",
                log.timestamp(), log.level(), log.logger(), log.message()));
        }
        return sb.toString();
    }

    public String buildFullAnalysisPrompt(List<LogEntry> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Java/Spring Boot log analyst.\n\n");
        sb.append("Perform a comprehensive analysis of these logs including:\n");
        sb.append("1. Summary of what happened\n");
        sb.append("2. Severity assessment (LOW, MEDIUM, HIGH, CRITICAL)\n");
        sb.append("3. Root causes for any errors\n");
        sb.append("4. Identified patterns\n");
        sb.append("5. Actionable suggestions\n\n");
        sb.append("LOGS:\n");
        for (LogEntry log : logs) {
            sb.append(String.format("[%s] %s %s - %s\n",
                log.timestamp(), log.level(), log.logger(), log.message()));
            if (log.stackTrace() != null && !log.stackTrace().isEmpty()) {
                sb.append("Stack Trace: ").append(log.stackTrace()).append("\n");
            }
        }
        return sb.toString();
    }
}

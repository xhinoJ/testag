package com.example.loganalyzer.model;

import java.time.LocalDateTime;

public record LogEntry(
    LocalDateTime timestamp,
    LogLevel level,
    String thread,
    String logger,
    String message,
    String stackTrace
) {
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}

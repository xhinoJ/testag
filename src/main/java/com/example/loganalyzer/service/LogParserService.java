package com.example.loganalyzer.service;

import com.example.loganalyzer.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogParserService {

    private static final Logger log = LoggerFactory.getLogger(LogParserService.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z?)\\s+" +
        "(TRACE|DEBUG|INFO|WARN|ERROR|FATAL)\\s+" +
        "\\[([^\\]]+)\\]\\s+" +
        "(\\S+)\\s*:\\s+(.*)"
    );

    public List<LogEntry> parse(String rawLogs) {
        List<LogEntry> entries = new ArrayList<>();
        String[] lines = rawLogs.split("\\r?\\n");

        StringBuilder currentStackTrace = new StringBuilder();
        LogEntry currentEntry = null;

        for (String line : lines) {
            Matcher matcher = LOG_LINE_PATTERN.matcher(line.trim());

            if (matcher.matches()) {
                if (currentEntry != null) {
                    entries.add(currentEntry);
                }

                LocalDateTime timestamp = parseTimestamp(matcher.group(1));
                LogEntry.LogLevel level;
                try {
                    level = LogEntry.LogLevel.valueOf(matcher.group(2));
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown log level '{}', defaulting to INFO", matcher.group(2));
                    level = LogEntry.LogLevel.INFO;
                }
                String thread = matcher.group(3);
                String logger = matcher.group(4);
                String message = matcher.group(5);

                currentEntry = new LogEntry(timestamp, level, thread, logger, message, null);
                currentStackTrace = new StringBuilder();
            } else if (currentEntry != null && !line.trim().isEmpty()) {
                currentStackTrace.append(line).append("\n");
            }
        }

        if (currentEntry != null) {
            String stackTrace = currentStackTrace.toString().trim();
            if (!stackTrace.isEmpty()) {
                currentEntry = new LogEntry(
                    currentEntry.timestamp(),
                    currentEntry.level(),
                    currentEntry.thread(),
                    currentEntry.logger(),
                    currentEntry.message(),
                    stackTrace
                );
            }
            entries.add(currentEntry);
        }

        return entries;
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse timestamp '{}', using current time", timestamp);
            return LocalDateTime.now();
        }
    }
}

package com.example.loganalyzer.service;

import com.example.loganalyzer.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogParserServiceTest {

    private final LogParserService parserService = new LogParserService();

    @Test
    void shouldParseStandardLogLines() {
        String rawLog = """
            2026-07-06T10:15:30.123Z  INFO  [main] o.s.b.w.e.tomcat.TomcatWebServer : Tomcat started on port 8080
            2026-07-06T10:15:31.456Z ERROR [http-nio-8080-exec-1] c.e.l.service.LogAnalysisService : Failed to process
            """;

        List<LogEntry> entries = parserService.parse(rawLog);

        assertFalse(entries.isEmpty());
        assertEquals(2, entries.size());
        assertEquals(LogEntry.LogLevel.INFO, entries.get(0).level());
        assertEquals(LogEntry.LogLevel.ERROR, entries.get(1).level());
    }

    @Test
    void shouldParseLogWithStackTrace() {
        String rawLog = """
            2026-07-06T10:15:30.123Z ERROR [main] c.e.l.service.UserService : Error occurred
            java.lang.NullPointerException: Object is null
                at com.example.loganalyzer.service.UserService.process(UserService.java:45)
                at com.example.loganalyzer.controller.UserController.handle(UserController.java:32)
            """;

        List<LogEntry> entries = parserService.parse(rawLog);

        assertEquals(1, entries.size());
        assertEquals(LogEntry.LogLevel.ERROR, entries.get(0).level());
        assertNotNull(entries.get(0).stackTrace());
        assertTrue(entries.get(0).stackTrace().contains("NullPointerException"));
    }

    @Test
    void shouldHandleEmptyLog() {
        List<LogEntry> entries = parserService.parse("");
        assertTrue(entries.isEmpty());
    }

    @Test
    void shouldParseMultipleLogEntries() {
        String rawLog = LogGenerator.generateNormalLogs(20);
        List<LogEntry> entries = parserService.parse(rawLog);
        assertEquals(20, entries.size());
    }
}

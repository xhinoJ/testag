package com.example.loganalyzer.service;

import com.example.loganalyzer.llm.LogAnalysisModel;
import com.example.loganalyzer.model.AnalysisOutput;
import com.example.loganalyzer.model.AnalysisType;
import com.example.loganalyzer.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalysisServiceModelTest {

    private static final AnalysisOutput OUTPUT = new AnalysisOutput(
        "summary text", "root cause text",
        List.of("fix it"), List.of("pattern a"));

    @Test
    void shouldUseInjectedModelAndProduceResult() {
        LogAnalysisModel model = prompt -> {
            assertTrue(prompt.contains("<LOG_DATA>"));
            return OUTPUT;
        };

        LogAnalysisService service = new LogAnalysisService(
            model, new LogParserService(), new PromptTemplateService());

        List<LogEntry> logs = List.of(
            new LogEntry(java.time.LocalDateTime.parse("2026-07-06T10:00:00"), LogEntry.LogLevel.ERROR,
                "main", "c.e.l.S", "boom", null));

        var result = service.analyze(logs, AnalysisType.FULL);

        assertEquals(AnalysisType.FULL, result.type());
        assertEquals("summary text", result.summary());
        assertEquals("root cause text", result.rootCause());
        assertEquals(List.of("fix it"), result.suggestions());
        assertEquals(List.of("pattern a"), result.patterns());
        assertEquals(1, result.errorCount());
        assertEquals(1, result.totalLogsAnalyzed());
    }
}

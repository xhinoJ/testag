package com.example.loganalyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateNormalLogs() {
        String logs = LogGenerator.generateNormalLogs(50);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains("INFO"));
        assertEquals(50, logs.lines().count());
    }

    @Test
    void shouldGenerateErrorLogs() {
        String logs = LogGenerator.generateErrorLogs(20);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains("ERROR"));
        assertTrue(logs.contains("Exception") || logs.contains("Error"));
    }

    @Test
    void shouldGeneratePerformanceLogs() {
        String logs = LogGenerator.generatePerformanceLogs(30);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains("WARN"));
        assertTrue(logs.contains("INFO"));
    }

    @Test
    void shouldGenerateMixedLogs() {
        String logs = LogGenerator.generateMixedLogs(100);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains("INFO"));
        assertTrue(logs.contains("WARN"));
        assertTrue(logs.contains("ERROR"));
    }

    @Test
    void shouldGenerateStacktraceLogs() {
        String logs = LogGenerator.generateStacktraceLogs(10);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains("ERROR"));
        assertTrue(logs.contains("at com.example") || logs.contains("at java.base"));
    }

    @Test
    void shouldGenerateTestFiles() throws IOException {
        Path outputDir = tempDir.resolve("test-logs");
        Files.createDirectories(outputDir);

        Files.writeString(outputDir.resolve("normal.log"), LogGenerator.generateNormalLogs(10));
        Files.writeString(outputDir.resolve("errors.log"), LogGenerator.generateErrorLogs(5));
        Files.writeString(outputDir.resolve("mixed.log"), LogGenerator.generateMixedLogs(20));

        assertTrue(Files.exists(outputDir.resolve("normal.log")));
        assertTrue(Files.exists(outputDir.resolve("errors.log")));
        assertTrue(Files.exists(outputDir.resolve("mixed.log")));

        assertTrue(Files.readString(outputDir.resolve("normal.log")).contains("INFO"));
        assertTrue(Files.readString(outputDir.resolve("errors.log")).contains("ERROR"));
    }
}

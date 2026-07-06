package com.example.loganalyzer.controller;

import com.example.loganalyzer.model.AnalysisResponse;
import com.example.loganalyzer.model.AnalysisType;
import com.example.loganalyzer.model.BatchAnalysisRequest;
import com.example.loganalyzer.model.BatchAnalysisResponse;
import com.example.loganalyzer.model.HealthResponse;
import com.example.loganalyzer.model.LogAnalysisRequest;
import com.example.loganalyzer.model.LogAnalysisResult;
import com.example.loganalyzer.model.LogEntry;
import com.example.loganalyzer.service.LogAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Analysis", description = "AI-powered log analysis endpoints")
public class LogAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisController.class);

    private final LogAnalysisService logAnalysisService;
    private final ExecutorService analysisExecutor = Executors.newFixedThreadPool(
        Math.min(Runtime.getRuntime().availableProcessors(), 8));

    public LogAnalysisController(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    @PostMapping("/analyze")
    @Operation(
        summary = "Analyze log content",
        description = "Send raw log content for AI-powered analysis including root cause detection, severity assessment, and actionable suggestions.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Analysis completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
        }
    )
    public ResponseEntity<AnalysisResponse> analyzeLogs(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Log content and analysis type",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Error Log Analysis",
                        summary = "Analyze a NullPointerException",
                        value = """
                            {
                                "logContent": "2026-07-06T10:15:30.123Z ERROR [http-nio-8080-exec-1] c.e.l.service.UserService : Failed to process user\\njava.lang.NullPointerException: Cannot invoke method on null object\\n    at com.example.loganalyzer.service.UserService.processUser(UserService.java:45)\\n    at com.example.loganalyzer.controller.UserController.handleRequest(UserController.java:32)",
                                "analysisType": "FULL"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody LogAnalysisRequest request) {
        log.info("Received log analysis request, type: {}", request.analysisType());

        List<LogEntry> entries = logAnalysisService.parseLogs(request.logContent());
        LogAnalysisResult result = logAnalysisService.analyze(entries, request.analysisType());
        return ResponseEntity.ok(AnalysisResponse.success(result));
    }

    @PostMapping("/batch")
    @Operation(
        summary = "Analyze multiple log sets",
        description = "Send multiple log contents for batch AI analysis. Each log is analyzed independently.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Batch analysis completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
        }
    )
    public ResponseEntity<BatchAnalysisResponse> analyzeBatch(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "List of log contents and analysis type",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Batch Analysis",
                        summary = "Analyze multiple error logs",
                        value = """
                            {
                                "logContents": [
                                    "2026-07-06T10:15:30.123Z ERROR [main] c.e.l.service.DBService : Connection failed\\njava.sql.SQLException: timeout after 30s\\n    at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:332)",
                                    "2026-07-06T10:15:31.456Z  WARN [main] c.e.l.service.CacheService : Cache miss rate high: 78%\\n2026-07-06T10:15:32.789Z ERROR [main] c.e.l.service.AuthService : Authentication failed\\norg.springframework.security.authentication.BadCredentialsException: Invalid credentials"
                                ],
                                "analysisType": "ROOT_CAUSE"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody BatchAnalysisRequest request) {
        log.info("Received batch analysis request, {} logs", request.logContents().size());

        List<CompletableFuture<LogAnalysisResult>> futures = request.logContents().stream()
            .map(content -> CompletableFuture.supplyAsync(() -> {
                List<LogEntry> entries = logAnalysisService.parseLogs(content);
                return logAnalysisService.analyze(entries, request.analysisType());
            }, analysisExecutor))
            .toList();

        List<LogAnalysisResult> results = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        return ResponseEntity.ok(BatchAnalysisResponse.success(results));
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Returns the current status of the log analysis service.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Service is running")
        }
    )
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("running"));
    }
}

# Spring AI Log Analyzer

AI-powered log analysis for Spring Boot applications using Spring AI with Ollama.

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring AI | 2.0.0 |
| springdoc-openapi | 3.0.3 |

## Prerequisites

- Java 21+
- Maven 3.8+
- Ollama (local LLM runtime)

## Setup

```bash
# Install and start Ollama
ollama serve & sleep 2 && ollama pull llama3.1

# Run the application
cd springailogs
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/logs/analyze` | Analyze a single log content |
| POST | `/api/logs/batch` | Analyze multiple log sets |
| GET | `/api/logs/health` | Health check |
| GET | `/swagger-ui.html` | Swagger UI (API documentation) |

## Usage Examples

### Single Analysis

```bash
curl -X POST http://localhost:8080/api/logs/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "logContent": "2026-07-06T10:15:30.123Z ERROR [main] c.e.l.service.UserService : Failed\njava.lang.NullPointerException: Object is null",
    "analysisType": "FULL"
  }'
```

### Batch Analysis

```bash
curl -X POST http://localhost:8080/api/logs/batch \
  -H "Content-Type: application/json" \
  -d '{
    "logContents": ["log set 1", "log set 2"],
    "analysisType": "ROOT_CAUSE"
  }'
```

### Analysis Types

- `SUMMARY` - Concise overview of log activity
- `ROOT_CAUSE` - Identify root causes for errors
- `PATTERNS` - Detect recurring patterns
- `FULL` - Comprehensive analysis (default)

## Test Logs

Generate test log files:

```bash
mvn exec:java -Dexec.mainClass="com.example.loganalyzer.service.LogGenerator" -Dexec.classpathScope=test
```

Test files are in `src/test/resources/test-logs/`:
- `normal-operations.log` - Standard INFO level logs
- `error-scenarios.log` - Various exception types
- `performance-issues.log` - Slow queries, memory issues
- `mixed-severity.log` - Mixed log levels
- `stacktraces.log` - Full stack traces

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: log-analyzer
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.1
        temperature: 0.3
        num-threads: 4

server:
  port: 8080

logging:
  level:
    com.example.loganalyzer: DEBUG
    org.springframework.ai: DEBUG
```

## Features

### Global Exception Handling

`GlobalExceptionHandler` provides structured error responses for validation errors, illegal arguments, and unexpected exceptions. Validation errors return `{"status": "error", "errors": {...}}` with per-field messages, while other errors return `{"status": "error", "message": "..."}`.

### Request Logging

`RequestLoggingInterceptor` logs HTTP method, URI, response status, and request duration (in milliseconds) for all `/api/logs/**` endpoints (excluding `/api/logs/health`).

### Input Truncation

`PromptTemplateService` enforces limits to prevent oversized prompts:
- **MAX_ENTRIES**: 200 log entries per prompt
- **MAX_ENTRY_CHARS**: 2000 characters per entry (longer entries are truncated)

### Prompt Injection Mitigation

Log data is wrapped in `<LOG_DATA>` / `</LOG_DATA>` delimiters with an explicit instruction guard to reduce the risk of prompt injection attacks.

### Windows Line Endings

`LogParserService` handles both Unix (`\n`) and Windows (`\r\n`) line endings.

### Graceful Handling of Unknown Log Levels

Unknown log levels in the input are logged as warnings and default to `INFO`.

## Project Structure

```
src/main/java/com/example/loganalyzer/
├── LoganalyzerApplication.java
├── config/
│   ├── AiConfig.java
│   ├── OpenApiConfig.java
│   ├── RequestLoggingInterceptor.java
│   └── WebConfig.java
├── controller/
│   └── LogAnalysisController.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── LogAnalysisException.java
├── model/
│   ├── AnalysisOutput.java
│   ├── AnalysisResponse.java
│   ├── AnalysisType.java
│   ├── BatchAnalysisRequest.java
│   ├── BatchAnalysisResponse.java
│   ├── HealthResponse.java
│   ├── LogAnalysisRequest.java
│   ├── LogAnalysisResult.java
│   └── LogEntry.java
└── service/
    ├── LogAnalysisService.java
    ├── LogParserService.java
    └── PromptTemplateService.java
```

```
src/test/java/com/example/loganalyzer/
├── config/
│   ├── RequestLoggingInterceptorTest.java
│   └── WebConfigIntegrationTest.java
└── service/
    ├── LogGenerator.java
    ├── LogGeneratorTest.java
    └── LogParserServiceTest.java
```

## Development Workflow

This project uses a two-agent development workflow:

- **@legendary-backend-engineer** - Implements features, creates PRs
- **@legendary-reviewer** - Reviews code, approves or requests changes

See [AGENTS.md](AGENTS.md) for details.

## License

Apache 2.0

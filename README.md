# Spring AI Log Analyzer

AI-powered log analysis for Spring Boot applications using Spring AI with Ollama.

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
mvn exec:java -Dexec.mainClass="com.example.loganalyzer.service.LogGenerator"
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
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.1
          temperature: 0.3
```

## Project Structure

```
src/main/java/com/example/loganalyzer/
├── LoganalyzerApplication.java
├── config/AiConfig.java
├── controller/LogAnalysisController.java
├── model/ (LogEntry, AnalysisType, LogAnalysisResult, etc.)
└── service/
    ├── LogAnalysisService.java
    ├── LogParserService.java
    └── PromptTemplateService.java
```

## License

Apache 2.0

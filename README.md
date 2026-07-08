# Spring AI Log Analyzer

AI-powered log analysis for Spring Boot applications using Spring AI with Ollama.

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 26 |
| Spring Boot | 4.1.0 |
| Spring AI | 2.0.0 |
| springdoc-openapi | 3.0.3 |

## Prerequisites

- Java 26+
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

## LLM Provider Configuration

The LLM layer is framework-agnostic behind the `LogAnalysisModel` interface. The active
implementation is selected via `app.ai.provider` (see `src/main/resources/application.yml`).

| `app.ai.provider` | Implementation | Backend |
|-------------------|---------------|---------|
| `ollama` (default) | `OllamaSpringAiModel` | Ollama (`http://localhost:11434`, model `llama3.1`) |
| `github-springai` | `GitHubModelsSpringAiModel` | GitHub Models (`https://models.inference.ai.azure.com`) via Spring AI OpenAI starter |
| `github-langchain4j` | `LangChain4jGitHubModelsModel` | GitHub Models (`https://models.inference.ai.azure.com`) via LangChain4j `OpenAiOfficialChatModel` |

Example — switch to GitHub Models (requires a `GITHUB_TOKEN` with `models:read`):

```bash
export GITHUB_TOKEN=ghp_xxx
mvn spring-boot:run -Dspring-boot.run.arguments=--app.ai.provider=github-springai
```

The GitHub Models endpoint is OpenAI-compatible; the `api-key`, `base-url`, and `model` are configured
under `app.ai.github-models` (Spring AI) or `app.ai.github-langchain4j` (LangChain4j) and consumed by the
respective model bean.

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

app:
  ai:
    provider: ollama                # ollama (default), github-springai, or github-langchain4j
    github-models:                  # used only when provider=github-springai
      base-url: https://models.inference.ai.azure.com
      api-key: ${GITHUB_TOKEN}
      model: openai/gpt-4o-mini
    github-langchain4j:             # used only when provider=github-langchain4j
      base-url: https://models.inference.ai.azure.com
      api-key: ${GITHUB_TOKEN}
      model: openai/gpt-4o-mini

server:
  port: 8080

logging:
  level:
    com.example.loganalyzer: DEBUG
    org.springframework.ai: INFO
```

### Switching LLM providers

The LLM layer is framework-agnostic via `LogAnalysisModel`. Select the backend with `app.ai.provider`:

- `ollama` (default) — local Ollama, no external credentials.
- `github-springai` — GitHub Models inference API (OpenAI-compatible) via Spring AI. Requires a `GITHUB_TOKEN` with the `models:read` scope exported in the environment.
- `github-langchain4j` — GitHub Models inference API via LangChain4j `OpenAiOfficialChatModel` (`isGitHubModels(true)`, model `openai/gpt-4o-mini`). Requires a `GITHUB_TOKEN` with the `models:read` scope.

When `provider` is `github-springai` or `github-langchain4j`, the application fails fast at startup with
`GITHUB_TOKEN is required for the github models provider` if the resolved `apiKey` is blank or still the
unresolved `${GITHUB_TOKEN}` placeholder. The `ollama` default needs no token.

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
│   ├── ExecutorConfig.java
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
└── llm/
    ├── LogAnalysisModel.java
    ├── OllamaSpringAiModel.java
    ├── GitHubModelsSpringAiModel.java
    └── LangChain4jGitHubModelsModel.java
```

```
src/test/java/com/example/loganalyzer/
├── config/
│   ├── ExecutorConfigTest.java
│   ├── RequestLoggingInterceptorTest.java
│   └── WebConfigIntegrationTest.java
└── service/
    ├── LogGenerator.java
    ├── LogGeneratorTest.java
    └── LogParserServiceTest.java
└── llm/
    ├── OllamaSpringAiModelTest.java
    ├── GitHubModelsSpringAiModelTest.java
    └── LangChain4jGitHubModelsModelTest.java
└── config/
    └── ProviderWiringIntegrationTest.java
```

## Development Workflow

This project uses a multi-agent development workflow:

- **@legendary-backend-engineer** - Implements features/fixes on a feature branch, creates PRs
- **@legendary-reviewer** - Reviews code, approves or requests changes
- **@legendary-tester** - Auto-triggered after merge to `main` for comprehensive QA

See [AGENTS.md](AGENTS.md) for details.

## License

Apache 2.0

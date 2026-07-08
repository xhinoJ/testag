# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Framework-agnostic `LogAnalysisModel` abstraction (`src/main/java/com/example/loganalyzer/llm/`)
- `OllamaSpringAiModel` implementation (default, wraps Ollama `ChatClient`)
- `GitHubModelsSpringAiModel` implementation (GitHub Models via `spring-ai-starter-model-openai`)
- `AppAiProperties` `@ConfigurationProperties` enum for validated `app.ai.provider` binding
- Provider selection via `app.ai.provider` (`ollama` default, `github-springai`, `github-langchain4j`) using `@ConditionalOnProperty`
- `LangChain4jGitHubModelsModel` implementation (GitHub Models via LangChain4j `OpenAiOfficialChatModel`, `isGitHubModels(true)`, model `openai/gpt-4o-mini`); parses JSON model output into `AnalysisOutput` with Jackson
- `langchain4j-open-ai-official` dependency (`langchain4j.version=1.13.1-beta23`); deprecated `langchain4j-github-models` intentionally not used
- Fail-fast startup when `github-springai`/`github-langchain4j` selected but `apiKey` is blank or the unresolved `${GITHUB_TOKEN}` placeholder (message: `GITHUB_TOKEN is required for the github models provider`)
- `OllamaSpringAiModelTest`, `GitHubModelsSpringAiModelTest`, `LangChain4jGitHubModelsModelTest`, `ProviderWiringIntegrationTest`, `LogAnalysisServiceModelTest`
- GlobalExceptionHandler for structured error responses
- RequestLoggingInterceptor for HTTP request/response logging
- Input truncation in PromptTemplateService (MAX_ENTRIES=200, MAX_ENTRY_CHARS=2000)
- Prompt injection mitigation with `<LOG_DATA>` delimiters
- Windows line endings support in LogParserService
- Graceful handling of unknown log levels
- WebConfig for interceptor registration
- OpenApiConfig for Swagger UI documentation
- AnalysisOutput record for structured AI responses
- RequestLoggingInterceptorTest
- WebConfigIntegrationTest
- LogGeneratorTest

### Fixed

- Resolved all 16 SpotBugs findings (`mvn spotbugs:check` now reports 0 bugs):
  - `EI_EXPOSE_REP`/`EI_EXPOSE_REP2`: model records (`AnalysisOutput`, `LogAnalysisResult`, `BatchAnalysisRequest`, `BatchAnalysisResponse`) now store and return immutable defensive copies via `List.copyOf(...)`
  - `EI_EXPOSE_REP2`: `LangChain4jGitHubModelsModel.objectMapper` and `LogAnalysisService.promptTemplateService` suppressed with `@SuppressFBWarnings` (thread-safe injected/shared collaborators, never mutated)
  - `REC_CATCH_EXCEPTION`: narrowed broad `catch (Exception e)` in `LangChain4jGitHubModelsModel.parse(String)` to `JsonProcessingException`
  - `VA_FORMAT_STRING_USES_NEWLINE`: `PromptTemplateService.appendEntries` now uses `System.lineSeparator()` / `%n` instead of literal `\n`
- Added `com.github.spotbugs:spotbugs-annotations` (provided) dependency to enable `@SuppressFBWarnings`
- Made SpotBugs a required CI gate: `spotbugs-maven-plugin` `failOnError` set to `true` and the `SpotBugs / Static Analysis` step in `qa-after-merge.yml` is now required (no longer `continue-on-error`)
- ChatClient bean injection (was bypassing configured bean)
- analysisType parameter now passed through from controller to service
- Division by zero in determineSeverity
- IndexOutOfBoundsException risk on empty log lists
- BatchAnalysisResponse.totalBatches renamed to totalResults
- Contradictory @NotNull validation in BatchAnalysisRequest

### Changed

- `LogAnalysisService` now depends on `LogAnalysisModel` instead of `ChatClient` directly (no framework coupling)
- `AiConfig` builds the Ollama `ChatClient` only when the Ollama provider is active and adds conditional GitHub Models beans for both `github-springai` and `github-langchain4j`
- Added `spring-ai-starter-model-openai` and `langchain4j-open-ai-official` dependencies; all OpenAI autoconfigurations excluded (models built conditionally/manually) so the Ollama default context starts without OpenAI/LangChain4j credentials
- GitHub Models credentials sourced via `app.ai.github-models.api-key` (`${GITHUB_TOKEN}`) through Spring property resolution, not `System.getenv`
- `LogAnalysisService` no longer double-wraps `LogAnalysisException` from model implementations
- `org.springframework.ai` logging default lowered from `DEBUG` to `INFO`
- `app.ai.provider=ollama` documented in `application.yml` as the working default
- Made JaCoCo coverage a required (non-advisory) gate; OWASP Dependency Check kept advisory in `qa-after-merge.yml`
- `ExecutorConfig` now sets `AwaitTerminationSeconds(30)` for graceful shutdown drain
- Added `ExecutorConfigTest` asserting the analysis executor is a managed `ThreadPoolTaskExecutor`
- Upgraded Java from 21 to 26
- Upgraded exec-maven-plugin from 3.5.0 to 3.6.3
- Upgraded Spring Boot from 3.5.3 to 4.1.0
- Upgraded Spring AI from 1.0.0 to 2.0.0
- Upgraded springdoc-openapi from 2.8.6 to 3.0.3
- Replaced parallelStream with bounded ExecutorService
- Refactored PromptTemplateService (DRY extraction of formatEntry and appendEntries)

## [0.0.1-SNAPSHOT] - 2026-07-06

### Added

- Initial project setup
- LogParserService for parsing structured log lines
- PromptTemplateService for building AI prompts
- LogAnalysisService for orchestrating log analysis
- LogAnalysisController with `/analyze`, `/batch`, `/health` endpoints
- OpenAPI/Swagger documentation
- LogGenerator for test data generation
- Unit tests for LogParserService

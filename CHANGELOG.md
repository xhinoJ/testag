# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

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

- ChatClient bean injection (was bypassing configured bean)
- analysisType parameter now passed through from controller to service
- Division by zero in determineSeverity
- IndexOutOfBoundsException risk on empty log lists
- BatchAnalysisResponse.totalBatches renamed to totalResults
- Contradictory @NotNull validation in BatchAnalysisRequest

### Changed

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

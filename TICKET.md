# Ticket: Upgrade Spring Boot and Spring AI to latest versions

**Priority:** High
**Description:** Upgrade Spring Boot from 3.5.3 to 4.1.0 and Spring AI from 1.0.0 to 2.0.0 GA.

## Current Versions
- Spring Boot: 3.5.3
- Spring AI: 1.0.0
- Java: 21

## Target Versions
- Spring Boot: 4.1.0
- Spring AI: 2.0.0 GA

## Breaking Changes to Handle

### Spring Boot 4.x
- Requires Java 17+ (project already uses Java 21, OK)
- Based on Spring Framework 7.0
- Some deprecated APIs removed

### Spring AI 2.0
- **Jackson 3 migration**: Uses `tools.jackson` package instead of `com.fasterxml.jackson`
- Package renames for MCP annotations
- ToolContext changes (conversation history removed)
- Review upgrade notes before implementing

## Requirements
1. Update `pom.xml` parent version to 4.1.0
2. Update `spring-ai.version` to 2.0.0
3. Handle any Jackson 3 migration if needed
4. Fix any compilation errors from API changes
5. Run `mvn compile -q` and `mvn test -q` to verify

## Acceptance Criteria
- Application compiles without errors
- All existing tests pass
- No regressions in functionality

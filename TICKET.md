# Ticket: Dependency Audit and Upgrade

**Priority:** Medium
**Description:** Audit all project dependencies against their latest available versions and upgrade any that are outdated.

## Current Dependency Versions

| Dependency | Current Version | Latest Available | Status |
|---|---|---|---|
| Spring Boot (parent POM) | 4.1.0 | 4.1.0 | Up to date |
| Spring AI | 2.0.0 | 2.0.0 | Up to date |
| springdoc-openapi | 3.0.3 | 3.0.3 | Up to date |
| exec-maven-plugin | 3.5.0 | 3.6.3 | **Outdated** |

## Requirements

1. Verify current versions for all dependencies declared in `pom.xml`
2. Check Maven Central / official sources for latest stable releases
3. Upgrade any outdated dependencies
4. Review changelogs for breaking changes before upgrading
5. Run `mvn compile -q` and `mvn test -q` to verify nothing is broken

## Acceptance Criteria

- All dependencies are at their latest stable versions
- Application compiles without errors
- All existing tests pass
- No regressions in functionality

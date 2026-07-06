# Ticket: Upgrade Java 21 to Java 26

**Priority:** Medium
**Description:** Upgrade the Java version target from 21 to 26 to match the installed JDK runtime.

## Current vs Target

| Property | Current | Target |
|---|---|---|
| `java.version` in `pom.xml` | 21 | 26 |
| Installed JDK | 26.0.1 | 26.0.1 |

## Requirements

1. Update `<java.version>` from `21` to `26` in `pom.xml`
2. Run `mvn compile -q` and `mvn test -q` to verify

## Acceptance Criteria

- Application compiles without errors
- All existing tests pass
- No regressions in functionality

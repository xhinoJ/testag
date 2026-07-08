# Ticket: Fix JaCoCo coverage on Java 26 (class file major version 70)

**Priority:** Critical
**Source:** Architect review C-1 (REVIEW.md)
**Description:** JaCoCo 0.8.13 cannot instrument Java 26 bytecode. During `mvn test` every fork logs `java.lang.IllegalArgumentException: Unsupported class file major version 70`, and the `jacoco:check` goal in CI (`qa-after-merge.yml`) either errors or reports zero/incorrect coverage. Coverage data is effectively broken on Java 26.

## Affected

- `pom.xml` — `jacoco-maven-plugin` version 0.8.13
- `.github/workflows/qa-after-merge.yml` — `jacoco:check` goal

## Requirements

1. Upgrade `jacoco-maven-plugin` to **0.8.14+** (release that adds Java 26 / class version 70 support).
2. Verify Pitest 1.19.1 and its bundled ASM can also read class file major version 70.
3. Run `mvn test` and confirm no `Unsupported class file major version 70` warnings and that `jacoco:check` produces real coverage.

## Acceptance Criteria

- `mvn test` runs without `Unsupported class file major version 70` warnings.
- JaCoCo coverage report reflects actual instrumented coverage.
- CI `qa-after-merge.yml` coverage gate passes with non-zero coverage.

# Ticket: Harden CI quality gates and executor shutdown (QA follow-up)

**Priority:** Medium
**Source:** Legendary Tester QA report (post-merge PR #2) — R-1, R-2, R-4
**Description:** Follow-up hardening after the critical C-1/C-2 fixes merged. Addresses process-level and resilience gaps noted in QA.

## Findings

- **R-1 (Medium):** All CI quality gates in `qa-after-merge.yml` used `continue-on-error: true`, so they report but cannot fail the build. The JaCoCo coverage gate (C-1 acceptance criterion) and OWASP CVE scan were advisory only.
- **R-2 (Low):** `ExecutorConfig` set `waitForTasksToCompleteOnShutdown(true)` but left default `awaitTerminationSeconds=0`, so shutdown signals but does not drain in-flight batch analyses.
- **R-4 (Low):** No test asserted the executor is Spring-managed; C-2 could silently regress.

## Requirements

1. Make `jacoco:check` and OWASP Dependency Check required (remove `continue-on-error`) in `qa-after-merge.yml`.
2. Set `executor.setAwaitTerminationSeconds(30)` in `ExecutorConfig`.
3. Add `ExecutorConfigTest` verifying `analysisExecutor` is a `ThreadPoolTaskExecutor` (max pool 8, `log-analysis-` prefix) and that `LogAnalysisController` is injected with it.

## Acceptance Criteria

- `mvn clean test jacoco:check` → BUILD SUCCESS (22 tests, coverage gate enforces).
- CI gates for coverage and OWASP are required steps.
- `ExecutorConfigTest` passes and locks in the managed-executor behavior.

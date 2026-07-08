# Ticket: Manage analysis ExecutorService lifecycle (thread leak)

**Priority:** Critical
**Source:** Architect review C-2 (REVIEW.md)
**Description:** `LogAnalysisController` creates a fixed thread pool as an instance field and never calls `shutdown()`. On Spring context refresh / redeploy the threads leak, and under dev reload the pools silently accumulate. Raw `Executors` pools should not be spawned inside a controller.

## Affected

- `src/main/java/com/example/loganalyzer/controller/LogAnalysisController.java:36-37` — `ExecutorService analysisExecutor = Executors.newFixedThreadPool(...)`

## Requirements

1. Move the executor out of the controller into a managed Spring bean (e.g. a `@Configuration` exposing `ThreadPoolTaskExecutor` / `Executor`).
2. Inject the managed `Executor`/`TaskExecutor` into `LogAnalysisController` via constructor injection.
3. Remove the raw `Executors.newFixedThreadPool(...)` instance field from the controller.

## Acceptance Criteria

- No `ExecutorService` is created as a controller instance field.
- The executor is a Spring-managed bean whose lifecycle (shutdown) is handled by the container.
- Existing batch endpoint behavior and thread bounds (max 8 threads) are preserved.
- `mvn compile -q` and `mvn test -q` succeed.

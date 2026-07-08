# Investigation Ticket: Framework-Agnostic LLM Abstraction (Ollama / GitHub Models / LangChain4j)

## Status
- **Type:** Investigation (no code changes in this ticket)
- **Branch:** `feature/ticket-github-models-investigation`
- **Related:** `TICKET-C1-jacoco-java26.md`, `TICKET-C2-executor-leak.md`

## 1. Background

Current implementation is hardwired to a single provider:

- `pom.xml` (line 50): `spring-ai-starter-model-ollama` (Spring AI 2.0.0)
- `src/main/java/com/example/loganalyzer/config/AiConfig.java`: builds a `ChatClient` bean
- `src/main/java/com/example/loganalyzer/service/LogAnalysisService.java`: consumes `ChatClient` directly

The goal of this ticket is to answer whether we can:

1. Switch the backend from Ollama to **GitHub Models** (the GitHub-hosted inference API, authenticated with a `GITHUB_TOKEN` — not the GitHub Copilot Chat product, which is not an embeddable backend).
2. Make the LLM layer **framework-agnostic**: an abstraction with multiple implementations (Spring AI + Ollama, Spring AI + GitHub Models, optionally LangChain4j + GitHub Models), selectable at runtime via a configuration parameter.

## 2. Can we be framework-agnostic with pluggable implementations selected by config?

**Yes.** Both candidate frameworks expose a minimal, near-identical surface for our use case (one prompt in, one string out):

- **Spring AI:** `chatClient.prompt().user(prompt).call().content()` → `String`
- **LangChain4j:** `chatLanguageModel.chat(prompt)` → `String` (or `ChatModel.chat(String)` convenience)

### Proposed design

```java
public interface LogAnalysisModel {
    String analyze(String prompt);
}
```

Implementations:

- `OllamaSpringAiModel` — wraps `spring-ai-starter-model-ollama` `ChatClient` (existing behavior).
- `GitHubModelsSpringAiModel` — wraps `spring-ai-starter-model-openai` `ChatClient` pointed at the GitHub Models endpoint.
- `GitHubModelsLangChain4jModel` (optional) — wraps LangChain4j `ChatLanguageModel` for GitHub Models.

Wiring:

- Select via a property, e.g. `app.ai.provider=ollama|github-springai|github-langchain4j`.
- Use Spring `@ConditionalOnProperty` (or profiles) so only the active implementation bean is created.
- `LogAnalysisService` depends solely on `LogAnalysisModel` — it never references `ChatClient` or `ChatLanguageModel` directly.

This keeps `LogAnalysisService` unchanged across provider/framework swaps.

## 3. Q1 — Does Spring AI support GitHub Models?

**Yes, indirectly (no native "Copilot" provider exists).**

- Spring AI has no dedicated "GitHub Models" or "Copilot" starter. Its provider matrix includes OpenAI, Azure OpenAI, Anthropic, Ollama, Vertex AI, Bedrock, DeepSeek, etc.
- GitHub Models is an **OpenAI-compatible** endpoint: `https://models.inference.ai.azure.com`, authenticated with a `GITHUB_TOKEN`.
- Therefore the existing `spring-ai-starter-model-openai` works by overriding `base-url` + `api-key`:

```yaml
spring:
  ai:
    openai:
      base-url: https://models.inference.ai.azure.com
      api-key: ${GITHUB_TOKEN}
      chat:
        options:
          model: gpt-4o-mini   # pin a specific model
```

No framework migration required.

## 4. Q2 — Does LangChain4j support GitHub Models?

**Yes, natively.**

- `langchain4j-github-models` module (`GitHubModelsChatModel` / `GitHubModelsStreamingChatModel`), authenticated with `GITHUB_TOKEN`. This module is now **deprecated** in favor of `langchain4j-open-ai-official` with `.isGitHubModels(true)` (which auto-sets the Azure inference `baseUrl`).
- Usage:

```java
ChatModel model = OpenAiOfficialChatModel.builder()
        .apiKey(System.getenv("GITHUB_TOKEN"))
        .modelName("gpt-4o-mini")
        .isGitHubModels(true)
        .build();
```

`GITHUB_TOKEN` is auto-detected from the environment in GitHub Actions / Codespaces.

## 5. Q3 — Migration to LangChain4j: pros & cons

**With the abstraction layer (Section 2), a full migration becomes unnecessary** — both can coexist. Recorded for completeness:

**Pros (LangChain4j)**

- Native GitHub Models module (until deprecated path).
- Richer RAG / agent / tooling ecosystem and larger community surface for advanced use cases.

**Cons (LangChain4j)**

- Full rewrite of `AiConfig` and `LogAnalysisService` (`ChatClient` → `ChatLanguageModel` / `AiServices`).
- Loss of current Spring Boot 4.1 auto-configuration integration.
- High churn for a single-purpose use case (one-shot structured log analysis).

**Conclusion:** Do not migrate away from Spring AI; instead keep it as the default and add LangChain4j only if a specific feature justifies it, behind the abstraction.

## 6. Abstraction layer: pros & cons

**Pros**

- Provider/framework swap with **zero** service-layer churn.
- Easy A/B comparison and environment-specific selection (local Ollama in dev, GitHub Models in CI/prod).
- Future-proof against provider/SDK changes.

**Cons**

- One extra interface + a thin wrapper per provider.
- If both frameworks are kept on the classpath, larger artifact and broader CVE/dependency surface (mitigated by conditional bean creation + optional dependencies).

## 7. Recommendation

1. Introduce the `LogAnalysisModel` abstraction.
2. Ship two implementations now: `ollama` (existing behavior, default) and `github-springai` (lowest risk — reuses Spring AI).
3. Add `github-langchain4j` only when a concrete LangChain4j feature is required.
4. Gate selection via `app.ai.provider` property with `@ConditionalOnProperty`.
5. Keep GitHub Models credentials in `GITHUB_TOKEN` (env/secret), never hardcoded or logged.

## 8. Risks & Notes

- **GitHub Models rate limits:** per-model daily quota (`UserByModelByDay`); returns HTTP 429. Quota is **separate** from any GitHub Copilot entitlement — a paid Copilot tier does not grant unlimited inference. Implement backoff/retry and surface 429s clearly.
- **Model pinning:** pin specific model versions (avoid floating aliases like `gpt-latest`); GitHub curates availability by region/entitlement.
- **Secret hygiene:** `GITHUB_TOKEN` must not appear in logs, config dumps, or exception messages.
- **Dependency hygiene:** use optional/conditional dependencies so the unused framework is not loaded in a given deployment.

## 9. Follow-up (if approved)

- New implementation ticket: "Add `LogAnalysisModel` abstraction + `ollama`/`github-springai` implementations behind `app.ai.provider`".
- New implementation ticket (optional): "Add `github-langchain4j` implementation".
- Update README provider/configuration docs and CHANGELOG once implemented.

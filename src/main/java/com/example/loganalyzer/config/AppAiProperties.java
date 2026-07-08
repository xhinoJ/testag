package com.example.loganalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.ai")
public class AppAiProperties {

    private final Provider provider;
    private final GitHubModels gitHubModels;
    private final GitHubLangChain4j gitHubLangChain4j;

    public AppAiProperties(@DefaultValue("ollama") Provider provider,
                           @DefaultValue GitHubModels gitHubModels,
                           @DefaultValue GitHubLangChain4j gitHubLangChain4j) {
        this.provider = provider;
        this.gitHubModels = gitHubModels;
        this.gitHubLangChain4j = gitHubLangChain4j;
    }

    public Provider getProvider() {
        return provider;
    }

    public GitHubModels getGitHubModels() {
        return gitHubModels;
    }

    public GitHubLangChain4j getGitHubLangChain4j() {
        return gitHubLangChain4j;
    }

    public enum Provider {
        OLLAMA,
        GITHUB_SPRINGAI,
        GITHUB_LANGCHAIN4J
    }

    public static class GitHubModels {

        private final String baseUrl;
        private final String apiKey;
        private final String model;

        public GitHubModels(@DefaultValue("https://models.inference.ai.azure.com") String baseUrl,
                            @DefaultValue("${GITHUB_TOKEN}") String apiKey,
                            @DefaultValue("openai/gpt-4o-mini") String model) {
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getModel() {
            return model;
        }
    }

    public static class GitHubLangChain4j {

        private final String baseUrl;
        private final String apiKey;
        private final String model;

        public GitHubLangChain4j(@DefaultValue("https://models.inference.ai.azure.com") String baseUrl,
                                 @DefaultValue("${GITHUB_TOKEN}") String apiKey,
                                 @DefaultValue("openai/gpt-4o-mini") String model) {
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getModel() {
            return model;
        }
    }
}

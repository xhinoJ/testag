package com.example.loganalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.ai")
public class AppAiProperties {

    private final Provider provider;
    private final GitHubModels githubModels;

    public AppAiProperties(@DefaultValue("ollama") Provider provider,
                           @DefaultValue GitHubModels githubModels) {
        this.provider = provider;
        this.githubModels = githubModels;
    }

    public Provider getProvider() {
        return provider;
    }

    public GitHubModels getGitHubModels() {
        return githubModels;
    }

    public enum Provider {
        OLLAMA,
        GITHUB_SPRINGAI
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
}

package com.example.loganalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.ai")
public class AppAiProperties {

    private final Provider provider;

    public AppAiProperties(@DefaultValue("ollama") Provider provider) {
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }

    public enum Provider {
        OLLAMA,
        GITHUB_SPRINGAI
    }
}

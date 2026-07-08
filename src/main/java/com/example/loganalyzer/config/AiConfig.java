package com.example.loganalyzer.config;

import com.example.loganalyzer.llm.GitHubModelsSpringAiModel;
import com.example.loganalyzer.llm.LangChain4jGitHubModelsModel;
import com.example.loganalyzer.llm.LogAnalysisModel;
import com.example.loganalyzer.llm.OllamaSpringAiModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppAiProperties.class)
public class AiConfig {

    private static final String SYSTEM_PROMPT = """
        You are an expert Java/Spring Boot log analyzer.
        You analyze application logs to identify errors, patterns, and provide actionable insights.
        Always respond in a structured and clear format.
        """;

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama", matchIfMissing = true)
    public ChatClient ollamaChatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem(SYSTEM_PROMPT)
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "github-springai")
    public OpenAiChatModel githubModelsChatModel(AppAiProperties properties) {
        requireGitHubToken(properties.getGitHubModels().getApiKey());
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .baseUrl(properties.getGitHubModels().getBaseUrl())
            .apiKey(properties.getGitHubModels().getApiKey())
            .gitHubModels(true)
            .model(properties.getGitHubModels().getModel())
            .build();
        return OpenAiChatModel.builder()
            .options(options)
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "github-springai")
    public ChatClient githubModelsChatClient(OpenAiChatModel githubModelsChatModel) {
        return ChatClient.builder(githubModelsChatModel)
            .defaultSystem(SYSTEM_PROMPT)
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "github-langchain4j")
    public OpenAiOfficialChatModel langChain4jGitHubModelsChatModel(AppAiProperties properties) {
        AppAiProperties.GitHubLangChain4j cfg = properties.getGitHubLangChain4j();
        requireGitHubToken(cfg.getApiKey());
        return OpenAiOfficialChatModel.builder()
            .baseUrl(cfg.getBaseUrl())
            .apiKey(cfg.getApiKey())
            .isGitHubModels(true)
            .modelName(cfg.getModel())
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama", matchIfMissing = true)
    public LogAnalysisModel ollamaLogAnalysisModel(ChatClient ollamaChatClient) {
        return new OllamaSpringAiModel(ollamaChatClient);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "github-springai")
    public LogAnalysisModel githubModelsLogAnalysisModel(ChatClient githubModelsChatClient) {
        return new GitHubModelsSpringAiModel(githubModelsChatClient);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "github-langchain4j")
    public LogAnalysisModel langChain4jGitHubModelsLogAnalysisModel(OpenAiOfficialChatModel chatModel,
                                                                     ObjectMapper objectMapper) {
        return new LangChain4jGitHubModelsModel(chatModel, objectMapper);
    }

    private void requireGitHubToken(String apiKey) {
        if (apiKey == null || apiKey.isBlank() || "${GITHUB_TOKEN}".equals(apiKey.trim())) {
            throw new IllegalStateException("GITHUB_TOKEN is required for the github models provider");
        }
    }
}

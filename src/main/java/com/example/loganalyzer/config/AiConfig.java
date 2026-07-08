package com.example.loganalyzer.config;

import com.example.loganalyzer.llm.GitHubModelsSpringAiModel;
import com.example.loganalyzer.llm.LogAnalysisModel;
import com.example.loganalyzer.llm.OllamaSpringAiModel;
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
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .baseUrl("https://models.inference.ai.azure.com")
            .apiKey(System.getenv("GITHUB_TOKEN"))
            .gitHubModels(true)
            .model("openai/gpt-4o-mini")
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
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama", matchIfMissing = true)
    public LogAnalysisModel ollamaLogAnalysisModel(ChatClient ollamaChatClient) {
        return new OllamaSpringAiModel(ollamaChatClient);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "github-springai")
    public LogAnalysisModel githubModelsLogAnalysisModel(ChatClient githubModelsChatClient) {
        return new GitHubModelsSpringAiModel(githubModelsChatClient);
    }
}

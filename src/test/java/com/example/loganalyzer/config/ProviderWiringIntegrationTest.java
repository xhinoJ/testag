package com.example.loganalyzer.config;

import com.example.loganalyzer.llm.GitHubModelsSpringAiModel;
import com.example.loganalyzer.llm.LangChain4jGitHubModelsModel;
import com.example.loganalyzer.llm.LogAnalysisModel;
import com.example.loganalyzer.llm.OllamaSpringAiModel;
import com.example.loganalyzer.model.AnalysisOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderWiringIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        ChatClient.Builder chatClientBuilder() {
            ChatClient.Builder builder = mock(ChatClient.Builder.class);
            ChatClient chatClient = mock(ChatClient.class);
            when(builder.defaultSystem(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(chatClient);
            return builder;
        }
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(AiConfig.class, TestConfig.class)
        .withPropertyValues("spring.ai.openai.enabled=false");

    @Test
    void defaultProviderShouldWireOllamaModel() {
        runner.withPropertyValues("app.ai.provider=ollama")
            .run(context -> {
                assertThat(context).hasSingleBean(LogAnalysisModel.class);
                assertThat(context.getBean(LogAnalysisModel.class)).isInstanceOf(OllamaSpringAiModel.class);
            });
    }

    @Test
    void missingProviderShouldDefaultToOllama() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(LogAnalysisModel.class);
            assertThat(context.getBean(LogAnalysisModel.class)).isInstanceOf(OllamaSpringAiModel.class);
        });
    }

    @Test
    void githubLangChain4jProviderShouldWireLangChain4jModelAndExcludeOllama() {
        runner.withPropertyValues(
                "app.ai.provider=github-langchain4j",
                "app.ai.github-langchain4j.api-key=dummy-token")
            .run(context -> {
                assertThat(context).hasSingleBean(LogAnalysisModel.class);
                assertThat(context.getBean(LogAnalysisModel.class)).isInstanceOf(LangChain4jGitHubModelsModel.class);
                assertThat(context).doesNotHaveBean(OllamaSpringAiModel.class);
            });
    }

    @Test
    void githubSpringAiProviderShouldWireGitHubModelsModel() {
        runner.withPropertyValues(
                "app.ai.provider=github-springai",
                "app.ai.github-models.api-key=dummy-token")
            .run(context -> {
                assertThat(context).hasSingleBean(LogAnalysisModel.class);
                assertThat(context.getBean(LogAnalysisModel.class)).isInstanceOf(GitHubModelsSpringAiModel.class);
            });
    }

    @Test
    void githubLangChain4jProviderWithoutTokenShouldFailToStart() {
        runner.withPropertyValues(
                "app.ai.provider=github-langchain4j",
                "app.ai.github-langchain4j.api-key=")
            .run(context -> assertThat(context).hasFailed());
    }
}

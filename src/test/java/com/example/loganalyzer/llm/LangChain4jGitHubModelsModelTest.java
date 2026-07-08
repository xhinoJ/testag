package com.example.loganalyzer.llm;

import com.example.loganalyzer.exception.LogAnalysisException;
import com.example.loganalyzer.model.AnalysisOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LangChain4jGitHubModelsModelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatModel chatModelReturning(String text) {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse response = mock(ChatResponse.class);
        when(response.aiMessage()).thenReturn(AiMessage.from(text));
        when(chatModel.chat(any(UserMessage.class))).thenReturn(response);
        return chatModel;
    }

    @Test
    void shouldParseWellFormedJsonIntoAnalysisOutput() {
        ChatModel chatModel = chatModelReturning(
            "{\"summary\":\"s\",\"rootCause\":\"r\",\"suggestions\":[\"a\",\"b\"],\"patterns\":[\"p\"]}");

        AnalysisOutput output = new LangChain4jGitHubModelsModel(chatModel, objectMapper).analyze("logs");

        assertThat(output.summary()).isEqualTo("s");
        assertThat(output.rootCause()).isEqualTo("r");
        assertThat(output.suggestions()).containsExactly("a", "b");
        assertThat(output.patterns()).containsExactly("p");
    }

    @Test
    void shouldStripMarkdownFencesAndUseDefaultsForMissingFields() {
        ChatModel chatModel = chatModelReturning("```json\n{\"summary\":\"only summary\"}\n```");

        AnalysisOutput output = new LangChain4jGitHubModelsModel(chatModel, objectMapper).analyze("logs");

        assertThat(output.summary()).isEqualTo("only summary");
        assertThat(output.rootCause()).isNull();
        assertThat(output.suggestions()).isEmpty();
        assertThat(output.patterns()).isEmpty();
    }

    @Test
    void shouldThrowWhenResponseIsNotJson() {
        ChatModel chatModel = chatModelReturning("not json at all");

        assertThatThrownBy(() -> new LangChain4jGitHubModelsModel(chatModel, objectMapper).analyze("logs"))
            .isInstanceOf(LogAnalysisException.class);
    }

    @Test
    void shouldThrowWhenChatModelFails() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat(any(UserMessage.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> new LangChain4jGitHubModelsModel(chatModel, objectMapper).analyze("logs"))
            .isInstanceOf(LogAnalysisException.class);
    }
}

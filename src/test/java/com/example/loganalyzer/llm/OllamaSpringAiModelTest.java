package com.example.loganalyzer.llm;

import com.example.loganalyzer.model.AnalysisOutput;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OllamaSpringAiModelTest {

    @Test
    void shouldReturnPopulatedAnalysisOutput() {
        AnalysisOutput expected = new AnalysisOutput("summary", "root",
            java.util.List.of("s1"), java.util.List.of("p1"));

        ChatClient.CallResponseSpec call = mock(ChatClient.CallResponseSpec.class);
        ChatClient.ChatClientRequestSpec req = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient chatClient = mock(ChatClient.class);

        when(chatClient.prompt()).thenReturn(req);
        when(req.user(anyString())).thenReturn(req);
        when(req.call()).thenReturn(call);
        when(call.entity(AnalysisOutput.class)).thenReturn(expected);

        OllamaSpringAiModel model = new OllamaSpringAiModel(chatClient);
        AnalysisOutput result = model.analyze("prompt");

        assertSame(expected, result);
        verify(req).user("prompt");
        verify(call).entity(AnalysisOutput.class);
    }
}

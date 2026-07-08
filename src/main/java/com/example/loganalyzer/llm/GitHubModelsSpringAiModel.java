package com.example.loganalyzer.llm;

import com.example.loganalyzer.exception.LogAnalysisException;
import com.example.loganalyzer.model.AnalysisOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

public class GitHubModelsSpringAiModel implements LogAnalysisModel {

    private static final Logger log = LoggerFactory.getLogger(GitHubModelsSpringAiModel.class);

    private final ChatClient chatClient;

    public GitHubModelsSpringAiModel(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public AnalysisOutput analyze(String prompt) {
        try {
            return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AnalysisOutput.class);
        } catch (Exception e) {
            log.error("GitHub Models analysis failed", e);
            throw new LogAnalysisException("AI service unavailable: " + e.getMessage(), e);
        }
    }
}

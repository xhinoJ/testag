package com.example.loganalyzer.llm;

import com.example.loganalyzer.exception.LogAnalysisException;
import com.example.loganalyzer.model.AnalysisOutput;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LangChain4jGitHubModelsModel implements LogAnalysisModel {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jGitHubModelsModel.class);

    private static final String SYSTEM_INSTRUCTION = """
        You are an expert Java/Spring Boot log analyzer.
        Analyze the provided logs and respond ONLY with a JSON object of the form:
        {"summary":"...","rootCause":"...","suggestions":["..."],"patterns":["..."]}.
        Do not include markdown fences or extra commentary.
        """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public LangChain4jGitHubModelsModel(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisOutput analyze(String prompt) {
        try {
            ChatResponse response = chatModel.chat(
                UserMessage.userMessage(SYSTEM_INSTRUCTION + "\n\n" + prompt));
            String text = response.aiMessage().text();
            return parse(text);
        } catch (LogAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("LangChain4j GitHub Models analysis failed", e);
            throw new LogAnalysisException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    private AnalysisOutput parse(String text) {
        String json = extractJson(text);
        try {
            OutputDto dto = objectMapper.readValue(json, OutputDto.class);
            return new AnalysisOutput(
                blankToNull(dto.summary()),
                blankToNull(dto.rootCause()),
                defaultList(dto.suggestions()),
                defaultList(dto.patterns()));
        } catch (Exception e) {
            log.error("Failed to parse LLM response into AnalysisOutput: {}", text, e);
            throw new LogAnalysisException("Unable to parse model response: " + e.getMessage(), e);
        }
    }

    private static String extractJson(String text) {
        if (text == null) {
            throw new LogAnalysisException("Empty model response", null);
        }
        String trimmed = text.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new LogAnalysisException("No JSON object found in model response", null);
        }
        return trimmed.substring(start, end + 1);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static List<String> defaultList(List<String> value) {
        if (value == null) {
            return new ArrayList<>();
        }
        return value.stream().filter(s -> s != null && !s.isBlank()).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OutputDto(String summary, String rootCause, List<String> suggestions, List<String> patterns) {
    }
}

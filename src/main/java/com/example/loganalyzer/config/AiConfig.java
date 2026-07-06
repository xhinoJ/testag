package com.example.loganalyzer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("""
                You are an expert Java/Spring Boot log analyzer.
                You analyze application logs to identify errors, patterns, and provide actionable insights.
                Always respond in a structured and clear format.
                """)
            .build();
    }
}

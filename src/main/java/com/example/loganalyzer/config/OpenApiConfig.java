package com.example.loganalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Spring AI Log Analyzer API")
                .description("AI-powered log analysis for Spring Boot applications. Analyze logs to detect errors, patterns, and get actionable insights using Ollama LLM.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Log Analyzer")
                    .url("https://github.com/example/loganalyzer")));
    }
}

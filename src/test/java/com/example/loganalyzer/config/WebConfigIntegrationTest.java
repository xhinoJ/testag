package com.example.loganalyzer.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.loganalyzer.controller.LogAnalysisController;
import com.example.loganalyzer.service.LogAnalysisService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogAnalysisController.class)
class WebConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogAnalysisService logAnalysisService;

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/logs/health"))
            .andExpect(status().isOk());
    }

    @Test
    void healthEndpointShouldNotBeLogged() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            mockMvc.perform(get("/api/logs/health"))
                .andExpect(status().isOk());

            boolean healthLogged = appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("uri=/api/logs/health"));
            assertFalse(healthLogged, "Health endpoint should NOT be logged by the interceptor");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void analyzeEndpointShouldBeLogged() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            mockMvc.perform(post("/api/logs/analyze")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"logContent\": \"test log\", \"analysisType\": \"FULL\"}"))
                .andExpect(status().isOk());

            boolean analyzeLogged = appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("uri=/api/logs/analyze"));
            assertTrue(analyzeLogged, "Analyze endpoint SHOULD be logged by the interceptor");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}

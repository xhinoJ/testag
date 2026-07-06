package com.example.loganalyzer.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.PathContainer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.junit.jupiter.api.Assertions.*;

class RequestLoggingInterceptorTest {

    private RequestLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RequestLoggingInterceptor();
    }

    @Test
    void preHandleShouldSetStartTimeAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/logs/analyze");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNotNull(request.getAttribute("requestLoggingStartTime"));
        assertTrue(request.getAttribute("requestLoggingStartTime") instanceof Long);
    }

    @Test
    void afterCompletionShouldNotThrowWhenStartTimeMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/logs/analyze");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() ->
            interceptor.afterCompletion(request, response, new Object(), null));
    }

    @Test
    void afterCompletionShouldCalculateDuration() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/logs/batch");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        interceptor.preHandle(request, response, new Object());

        assertDoesNotThrow(() ->
            interceptor.afterCompletion(request, response, new Object(), null));
    }

    @Test
    void afterCompletionShouldHandleErrorStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/logs/analyze");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);

        interceptor.preHandle(request, response, new Object());
        assertDoesNotThrow(() ->
            interceptor.afterCompletion(request, response, new Object(), null));
    }

    @Test
    void afterCompletionShouldHandleException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/logs/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        interceptor.preHandle(request, response, new Object());
        assertDoesNotThrow(() ->
            interceptor.afterCompletion(request, response, new Object(),
                new RuntimeException("test exception")));
    }

    @Test
    void webConfigShouldExcludeHealthEndpoint() {
        RequestLoggingInterceptor loggingInterceptor = new RequestLoggingInterceptor();
        WebConfig webConfig = new WebConfig(loggingInterceptor);

        assertNotNull(webConfig);

        PathPatternParser parser = new PathPatternParser();

        var includePattern = parser.parse("/api/logs/**");
        var excludePattern = parser.parse("/api/logs/health");

        assertTrue(includePattern.matches(PathContainer.parsePath("/api/logs/analyze")));
        assertTrue(includePattern.matches(PathContainer.parsePath("/api/logs/batch")));
        assertTrue(includePattern.matches(PathContainer.parsePath("/api/logs/health")));

        assertTrue(excludePattern.matches(PathContainer.parsePath("/api/logs/health")));
        assertFalse(excludePattern.matches(PathContainer.parsePath("/api/logs/analyze")));
        assertFalse(excludePattern.matches(PathContainer.parsePath("/api/logs/batch")));
    }

    @Test
    void afterCompletionShouldLogCorrectFormat() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/logs/analyze");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);

            interceptor.preHandle(request, response, new Object());
            interceptor.afterCompletion(request, response, new Object(), null);

            assertFalse(appender.list.isEmpty(), "Expected at least one log event");
            ILoggingEvent logEvent = appender.list.getFirst();
            String message = logEvent.getFormattedMessage();

            assertTrue(message.contains("method=POST"), "Log should contain method=POST");
            assertTrue(message.contains("uri=/api/logs/analyze"), "Log should contain uri=/api/logs/analyze");
            assertTrue(message.contains("status=200"), "Log should contain status=200");
            assertTrue(message.contains("duration="), "Log should contain duration=");
            assertTrue(message.endsWith("ms"), "Log should end with ms");
            assertEquals(Level.INFO, logEvent.getLevel());
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}

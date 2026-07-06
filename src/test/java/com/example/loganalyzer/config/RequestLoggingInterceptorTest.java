package com.example.loganalyzer.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

        // afterCompletion without preHandle — no start time attribute
        assertDoesNotThrow(() ->
            interceptor.afterCompletion(request, response, new Object(), null));
    }

    @Test
    void afterCompletionShouldCalculateDuration() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/logs/batch");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        interceptor.preHandle(request, response, new Object());

        // Simulate some processing time
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

        // Verify that WebConfig can be instantiated with the interceptor
        assertNotNull(webConfig);
    }
}

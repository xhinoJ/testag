package com.example.loganalyzer.config;

import com.example.loganalyzer.controller.LogAnalysisController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ExecutorConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private LogAnalysisController controller;

    @Test
    void analysisExecutorBeanIsManagedThreadPoolTaskExecutor() {
        assertTrue(context.containsBean("analysisExecutor"), "analysisExecutor bean must be registered");
        Executor executor = context.getBean("analysisExecutor", Executor.class);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor,
            "analysisExecutor must be a Spring-managed ThreadPoolTaskExecutor (no raw Executors pool)");

        ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
        assertEquals(8, pool.getMaxPoolSize(), "max pool size must be bounded at 8");
        assertNotNull(pool.getThreadNamePrefix());
        assertTrue(pool.getThreadNamePrefix().contains("log-analysis-"),
            "thread name prefix should identify the analysis pool");
    }

    @Test
    void controllerUsesManagedExecutorNotRawPool() throws Exception {
        java.lang.reflect.Field field = LogAnalysisController.class.getDeclaredField("analysisExecutor");
        field.setAccessible(true);
        Object controllerExecutor = field.get(controller);

        Executor managedExecutor = context.getBean("analysisExecutor", Executor.class);
        assertFalse(controllerExecutor.getClass().getSimpleName().contains("DelegatedExecutorService"),
            "controller must not hold a raw Executors-wrapped pool");
        assertEquals(managedExecutor, controllerExecutor,
            "controller must be injected with the container-managed analysisExecutor bean");
    }
}

package com.example.loganalyzer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LogGenerator {

    private static final DateTimeFormatter TS =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static final String[] NORMAL_MESSAGES = {
        "Application started successfully",
        "Processing request for user {}",
        "Database connection established",
        "Cache refreshed successfully",
        "Health check passed",
        "Request completed in {}ms",
        "Configuration loaded from application.yml",
        "Bean {} initialized",
        "Scheduled task executed: cleanupJob",
        "Response sent to client"
    };

    private static final String[] WARN_MESSAGES = {
        "Log content exceeds recommended size: {} bytes",
        "Slow query detected: {}ms (threshold: 1000ms)",
        "Connection pool running low: {} available",
        "Retry attempt {} for external service call",
        "Memory usage approaching threshold: {}%",
        "Thread pool nearing capacity: {} active threads",
        "Deprecated API usage detected: {}",
        "Rate limit approaching for client: {}"
    };

    private static final String[] ERROR_MESSAGES = {
        "Failed to process request: NullPointerException",
        "Database connection failed: timeout after 30s",
        "External service unavailable: HTTP 503",
        "SQL exception: table column not found",
        "OutOfMemoryError: Java heap space",
        "ClassNotFoundException: {}",
        "Authentication failed for user: {}",
        "File not found: /data/logs/archive.log"
    };

    private static final String[] LOGGERS = {
        "com.example.service.UserService",
        "com.example.service.OrderService",
        "com.example.service.PaymentService",
        "com.example.controller.UserController",
        "com.example.controller.OrderController",
        "com.example.repository.UserRepository",
        "com.example.config.DataSourceConfig",
        "org.springframework.web.servlet.DispatcherServlet",
        "org.hibernate.SQL",
        "com.zaxxer.hikari.HikariPool"
    };

    private static final String[] THREADS = {
        "main", "http-nio-8080-exec-1", "http-nio-8080-exec-2",
        "http-nio-8080-exec-3", "scheduling-1", "task-1", "task-2"
    };

    private static final String[] STACK_TRACES = {
        """
        java.lang.NullPointerException: Cannot invoke method on null object
            at com.example.service.UserService.processUser(UserService.java:45)
            at com.example.controller.UserController.handleRequest(UserController.java:32)
            at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:897)
        """,
        """
        org.springframework.dao.DataAccessResourceFailureException: Unable to acquire JDBC Connection
            at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:277)
            at com.example.repository.UserRepository.findById(UserRepository.java:23)
        """,
        """
        java.sql.SQLSyntaxErrorException: Table 'users' doesn't exist
            at com.mysql.cj.jdbc.exports.ResultSetImpl.findColumn(ResultSetImpl.java:316)
            at org.hibernate.loader.Loader.getColumnAliases(Loader.java:1234)
        """,
        """
        java.lang.OutOfMemoryError: Java heap space
            at java.base/java.util.Arrays.copyOf(Arrays.java:3512)
            at java.base/java.util.ArrayList.grow(ArrayList.java:237)
            at com.example.service.BatchProcessor.processBatch(BatchProcessor.java:89)
        """,
        """
        org.springframework.web.client.ResourceAccessException: I/O error: Connection refused
            at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:123)
            at com.example.client.ExternalApiClient.callService(ExternalApiClient.java:56)
        """
    };

    public static String generateNormalLogs(int count) {
        StringBuilder sb = new StringBuilder();
        LocalDateTime baseTime = LocalDateTime.now().minusHours(1);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = baseTime.plusSeconds(i * 10L);
            String message = NORMAL_MESSAGES[ThreadLocalRandom.current().nextInt(NORMAL_MESSAGES.length)];
            String logger = LOGGERS[ThreadLocalRandom.current().nextInt(LOGGERS.length)];
            String thread = THREADS[ThreadLocalRandom.current().nextInt(THREADS.length)];

            sb.append(String.format("%s  INFO  [%s] %s : %s%n",
                ts.format(TS), thread, logger, message));
        }
        return sb.toString();
    }

    public static String generateErrorLogs(int count) {
        StringBuilder sb = new StringBuilder();
        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(30);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = baseTime.plusSeconds(i * 5L);
            String message = ERROR_MESSAGES[ThreadLocalRandom.current().nextInt(ERROR_MESSAGES.length)];
            String logger = LOGGERS[ThreadLocalRandom.current().nextInt(LOGGERS.length)];
            String thread = THREADS[ThreadLocalRandom.current().nextInt(THREADS.length)];

            sb.append(String.format("%s ERROR [%s] %s : %s%n",
                ts.format(TS), thread, logger, message));

            if (ThreadLocalRandom.current().nextBoolean()) {
                String stackTrace = STACK_TRACES[ThreadLocalRandom.current().nextInt(STACK_TRACES.length)];
                sb.append(stackTrace).append("\n");
            }
        }
        return sb.toString();
    }

    public static String generatePerformanceLogs(int count) {
        StringBuilder sb = new StringBuilder();
        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(15);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = baseTime.plusSeconds(i * 3L);
            String logger = LOGGERS[ThreadLocalRandom.current().nextInt(LOGGERS.length)];
            String thread = THREADS[ThreadLocalRandom.current().nextInt(THREADS.length)];

            sb.append(String.format("%s  INFO  [%s] %s : Starting batch processing%n",
                ts.format(TS), thread, logger));

            int slowCount = ThreadLocalRandom.current().nextInt(1, 4);
            for (int j = 0; j < slowCount; j++) {
                LocalDateTime warnTs = ts.plusSeconds(j * 2L);
                String warnMsg = WARN_MESSAGES[ThreadLocalRandom.current().nextInt(WARN_MESSAGES.length)];
                sb.append(String.format("%s  WARN [%s] %s : %s%n",
                    warnTs.format(TS), thread, logger, warnMsg));
            }

            if (ThreadLocalRandom.current().nextDouble() > 0.7) {
                LocalDateTime errTs = ts.plusSeconds(5L);
                String errMsg = ERROR_MESSAGES[ThreadLocalRandom.current().nextInt(ERROR_MESSAGES.length)];
                sb.append(String.format("%s ERROR [%s] %s : %s%n",
                    errTs.format(TS), thread, logger, errMsg));
            }
        }
        return sb.toString();
    }

    public static String generateMixedLogs(int count) {
        StringBuilder sb = new StringBuilder();
        LocalDateTime baseTime = LocalDateTime.now().minusHours(2);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = baseTime.plusSeconds(i * 8L);
            double rand = ThreadLocalRandom.current().nextDouble();
            String logger = LOGGERS[ThreadLocalRandom.current().nextInt(LOGGERS.length)];
            String thread = THREADS[ThreadLocalRandom.current().nextInt(THREADS.length)];

            if (rand < 0.5) {
                String msg = NORMAL_MESSAGES[ThreadLocalRandom.current().nextInt(NORMAL_MESSAGES.length)];
                sb.append(String.format("%s  INFO  [%s] %s : %s%n",
                    ts.format(TS), thread, logger, msg));
            } else if (rand < 0.8) {
                String msg = WARN_MESSAGES[ThreadLocalRandom.current().nextInt(WARN_MESSAGES.length)];
                sb.append(String.format("%s  WARN [%s] %s : %s%n",
                    ts.format(TS), thread, logger, msg));
            } else {
                String msg = ERROR_MESSAGES[ThreadLocalRandom.current().nextInt(ERROR_MESSAGES.length)];
                sb.append(String.format("%s ERROR [%s] %s : %s%n",
                    ts.format(TS), thread, logger, msg));

                if (ThreadLocalRandom.current().nextBoolean()) {
                    String st = STACK_TRACES[ThreadLocalRandom.current().nextInt(STACK_TRACES.length)];
                    sb.append(st).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public static String generateStacktraceLogs(int count) {
        StringBuilder sb = new StringBuilder();
        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(10);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = baseTime.plusSeconds(i * 7L);
            String logger = LOGGERS[ThreadLocalRandom.current().nextInt(LOGGERS.length)];
            String thread = THREADS[ThreadLocalRandom.current().nextInt(THREADS.length)];
            String st = STACK_TRACES[ThreadLocalRandom.current().nextInt(STACK_TRACES.length)];

            sb.append(String.format("%s ERROR [%s] %s : Exception occurred%n",
                ts.format(TS), thread, logger));
            sb.append(st).append("\n");
        }
        return sb.toString();
    }

    public static void generateTestFiles() throws IOException {
        Path outputDir = Paths.get("src/test/resources/test-logs");
        Files.createDirectories(outputDir);

        Files.writeString(outputDir.resolve("normal-operations.log"), generateNormalLogs(200));
        Files.writeString(outputDir.resolve("error-scenarios.log"), generateErrorLogs(100));
        Files.writeString(outputDir.resolve("performance-issues.log"), generatePerformanceLogs(100));
        Files.writeString(outputDir.resolve("mixed-severity.log"), generateMixedLogs(300));
        Files.writeString(outputDir.resolve("stacktraces.log"), generateStacktraceLogs(50));
    }

    public static void main(String[] args) throws IOException {
        generateTestFiles();
        System.out.println("Test log files generated in src/test/resources/test-logs/");
    }
}

package com.example.loganalyzer.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record HealthResponse(
    @Schema(description = "Service status", example = "running")
    String status
) {}

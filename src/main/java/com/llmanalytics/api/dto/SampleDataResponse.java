package com.llmanalytics.api.dto;

public record SampleDataResponse(
        int projectCount,
        int experimentCount,
        int iterationCount,
        int interactionCount,
        String message
) {}

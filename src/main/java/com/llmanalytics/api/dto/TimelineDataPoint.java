package com.llmanalytics.api.dto;

public record TimelineDataPoint(
        String bucket,
        long interactionCount,
        Double avgLatencyMs,
        Long totalTokens
) {}

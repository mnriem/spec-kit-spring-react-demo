package com.llmanalytics.api.dto;

public record ToolUsageEntry(
        String toolName,
        long usageCount
) {}

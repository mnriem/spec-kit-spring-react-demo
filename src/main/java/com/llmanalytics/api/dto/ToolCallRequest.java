package com.llmanalytics.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Map;

public record ToolCallRequest(
        @NotBlank @Size(max = 255) String toolName,
        Map<String, Object> inputArguments,
        Map<String, Object> output,
        OffsetDateTime calledAt
) {}

package com.llmanalytics.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ToolCallResponse(
        UUID id,
        String toolName,
        String inputArguments,
        String output,
        Integer sequenceOrder,
        OffsetDateTime calledAt
) {}

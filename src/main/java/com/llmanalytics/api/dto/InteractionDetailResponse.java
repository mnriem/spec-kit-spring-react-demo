package com.llmanalytics.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record InteractionDetailResponse(
        UUID id,
        UUID iterationId,
        String iterationName,
        UUID experimentId,
        String experimentName,
        UUID projectId,
        String projectName,
        String model,
        Integer tokensIn,
        Integer tokensOut,
        Integer totalTokens,
        Long latencyMs,
        Double tokensPerSecond,
        BigDecimal estimatedCost,
        Integer toolCallCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime createdAt,
        String prompt,
        String responseMetadata,
        List<ToolCallResponse> toolCalls
) {}

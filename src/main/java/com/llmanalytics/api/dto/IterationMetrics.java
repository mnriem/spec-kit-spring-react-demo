package com.llmanalytics.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record IterationMetrics(
        UUID iterationId,
        String iterationName,
        Double avgLatencyMs,
        Double avgTokensIn,
        Double avgTokensOut,
        Double avgTotalTokens,
        Double toolCallRate,
        BigDecimal avgEstimatedCost,
        Long interactionCount
) {}

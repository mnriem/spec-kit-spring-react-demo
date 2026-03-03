package com.llmanalytics.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DashboardSummaryResponse(
        long totalInteractions,
        Double avgLatencyMs,
        Long totalTokens,
        BigDecimal totalEstimatedCost,
        OffsetDateTime from,
        OffsetDateTime to
) {}

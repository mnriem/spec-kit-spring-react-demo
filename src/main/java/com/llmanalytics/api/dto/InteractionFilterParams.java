package com.llmanalytics.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InteractionFilterParams(
        String model,
        UUID experimentId,
        UUID iterationId,
        UUID projectId,
        OffsetDateTime from,
        OffsetDateTime to,
        Long minLatencyMs,
        Long maxLatencyMs,
        int page,
        int size,
        String sort
) {}

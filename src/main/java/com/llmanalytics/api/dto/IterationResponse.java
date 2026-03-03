package com.llmanalytics.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IterationResponse(
        UUID id,
        UUID experimentId,
        String name,
        String description,
        OffsetDateTime createdAt
) {}

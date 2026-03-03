package com.llmanalytics.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExperimentResponse(
        UUID id,
        UUID projectId,
        String name,
        String description,
        OffsetDateTime createdAt,
        long iterationCount
) {}

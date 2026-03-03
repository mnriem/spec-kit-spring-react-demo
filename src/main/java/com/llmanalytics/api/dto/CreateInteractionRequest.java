package com.llmanalytics.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record CreateInteractionRequest(
        @NotBlank @Size(max = 255) String projectName,
        String projectDescription,
        @NotBlank @Size(max = 255) String experimentName,
        String experimentDescription,
        @NotBlank @Size(max = 255) String iterationName,
        String iterationDescription,
        @NotBlank @Size(max = 100) String model,
        String prompt,
        Map<String, Object> responseMetadata,
        @NotNull @Min(0) Integer tokensIn,
        @NotNull @Min(0) Integer tokensOut,
        @NotNull OffsetDateTime startedAt,
        @NotNull OffsetDateTime endedAt,
        @Valid List<ToolCallRequest> toolsCalled
) {}

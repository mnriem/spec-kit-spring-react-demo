package com.llmanalytics.api.dto;

public record TokensByModelEntry(
        String model,
        Long totalTokensIn,
        Long totalTokensOut
) {}

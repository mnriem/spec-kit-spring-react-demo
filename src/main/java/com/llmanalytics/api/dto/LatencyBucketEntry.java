package com.llmanalytics.api.dto;

public record LatencyBucketEntry(
        long bucketMin,
        long bucketMax,
        long count
) {}

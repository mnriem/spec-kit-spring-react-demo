package com.llmanalytics.api;

import java.time.Instant;

public record SuccessEnvelope<T>(T data, Instant timestamp) {
    public static <T> SuccessEnvelope<T> of(T data) {
        return new SuccessEnvelope<>(data, Instant.now());
    }
}

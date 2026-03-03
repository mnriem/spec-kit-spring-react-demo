package com.llmanalytics.api;

import java.time.Instant;
import java.util.List;

public record ErrorEnvelope(String error, String message, List<FieldError> fieldErrors, Instant timestamp) {
    public static ErrorEnvelope of(String error, String message) {
        return new ErrorEnvelope(error, message, null, Instant.now());
    }

    public static ErrorEnvelope withFieldErrors(String error, String message, List<FieldError> fieldErrors) {
        return new ErrorEnvelope(error, message, fieldErrors, Instant.now());
    }
}

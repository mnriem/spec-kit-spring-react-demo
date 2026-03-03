package com.llmanalytics.domain.service.exception;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String message) {
        super(message);
    }
}

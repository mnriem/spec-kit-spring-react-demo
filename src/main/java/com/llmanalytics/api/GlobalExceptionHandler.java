package com.llmanalytics.api;

import com.llmanalytics.domain.service.exception.DuplicateNameException;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorEnvelope.withFieldErrors("VALIDATION_ERROR", "Request validation failed", fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorEnvelope.withFieldErrors("VALIDATION_ERROR", "Constraint violation", fieldErrors));
    }

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorEnvelope> handleDuplicate(DuplicateNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorEnvelope.of("DUPLICATE_NAME", ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorEnvelope> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorEnvelope.of("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorEnvelope> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorEnvelope.of("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorEnvelope.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}

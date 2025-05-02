package ca.zhoozhoo.loaddev.loads.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(NumberFormatException.class)
    public Mono<ResponseEntity<String>> handleNumberFormatException(NumberFormatException ex) {
        log.error("Number format error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(BAD_REQUEST)
                .body("Invalid number format: Please provide valid numeric values"));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());
        String error = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> {
                    String field = err.getField();
                    String message = err.getDefaultMessage();
                    Object rejectedValue = err.getRejectedValue();
                    return String.format("%s: %s (rejected value: %s)", field, message, rejectedValue);
                })
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return Mono.just(ResponseEntity.status(BAD_REQUEST).body(error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(CONFLICT).body("Database error: " + ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<String>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(FORBIDDEN).body("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return Mono.just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<String>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(ex.getStatusCode())
                .body(ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }
}

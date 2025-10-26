package ca.zhoozhoo.loaddev.components.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static reactor.core.publisher.Mono.just;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the components service.
 * <p>
 * This class provides centralized exception handling for all REST controllers in the
 * components service. It handles common exceptions like validation errors, data integrity
 * violations, access denied, and general server errors, returning appropriate HTTP status
 * codes and error messages.
 * </p>
 *
 * @author Zhubin Salehi
 */
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(NumberFormatException.class)
    public Mono<ResponseEntity<String>> handleNumberFormatException(NumberFormatException ex) {
        log.error("Number format error: {}", ex.getMessage());
        return just(ResponseEntity.status(BAD_REQUEST)
                .body("Invalid number format: Please provide valid numeric values"));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());
        String error = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> "%s: %s (rejected value: %s)".formatted(
                        err.getField(),
                        err.getDefaultMessage(),
                        err.getRejectedValue()))
                .reduce((a, b) -> "%s; %s".formatted(a, b))
                .orElse("Validation failed");
        return just(ResponseEntity.status(BAD_REQUEST).body(error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return just(ResponseEntity.status(CONFLICT).body("Database error: " + ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<String>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return just(ResponseEntity.status(FORBIDDEN).body("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<String>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException: {}", ex.getMessage());
        return just(ResponseEntity.status(ex.getStatusCode())
                .body(ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }
}

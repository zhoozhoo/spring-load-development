package ca.zhoozhoo.loaddev.components.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static reactor.core.publisher.Mono.just;

import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

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
        log.error("Number format error: {}", ex.getMessage(), ex);

        return just(ResponseEntity.status(BAD_REQUEST)
                .body("Invalid number format: Please provide valid numeric values"));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<String>> handleServerWebInputException(ServerWebInputException ex) {
        log.error("HTTP message decoding error", ex);
        
        var cause = ex.getCause();
        if (cause != null) {
            log.error("Caused by: {}", cause.getClass().getName(), cause);
        }
        
        var errorMessage = "Failed to read HTTP message: " + ex.getReason();
        if (cause != null) {
            errorMessage += " - " + cause.getMessage();
        }
        
        return just(ResponseEntity.status(BAD_REQUEST).body(errorMessage));
    }

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<String>> handleDecodingException(DecodingException ex) {
        log.error("Decoding error occurred", ex);
        
        var cause = ex.getCause();
        var errorMessage = "Failed to decode request body";
        
        if (cause != null) {
            log.error("Root cause: {}", cause.getClass().getName(), cause);
            errorMessage += ": " + cause.getMessage();
        }
        
        return just(ResponseEntity.status(BAD_REQUEST).body(errorMessage));
    }

    @ExceptionHandler({InvalidFormatException.class, MismatchedInputException.class})
    public Mono<ResponseEntity<String>> handleJacksonException(JsonMappingException ex) {
        log.error("JSON mapping error occurred", ex);
        
        var errorMessage = new StringBuilder("JSON deserialization error");
        
        if (!ex.getPath().isEmpty()) {
            var path = ex.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .reduce((a, b) -> a + "." + b)
                    .orElse("unknown");
            errorMessage.append(" at field: ").append(path);
            log.error("Error at field path: {}", path);
        }
        
        if (ex instanceof InvalidFormatException invalidFormat) {
            errorMessage.append(". Invalid value: ")
                    .append(invalidFormat.getValue())
                    .append(", expected type: ")
                    .append(invalidFormat.getTargetType().getSimpleName());
            log.error("Invalid format - value: {}, target type: {}", 
                    invalidFormat.getValue(), invalidFormat.getTargetType());
        }
        
        return just(ResponseEntity.status(BAD_REQUEST).body(errorMessage.toString()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation error occurred", ex);
        
        var error = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> "%s: %s (rejected value: %s)".formatted(
                        err.getField(),
                        err.getDefaultMessage(),
                        err.getRejectedValue()))
                .reduce((a, b) -> "%s; %s".formatted(a, b))
                .orElse("Validation failed");
        
        log.error("Validation errors: {}", error);
        
        return just(ResponseEntity.status(BAD_REQUEST).body(error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation occurred", ex);

        return just(ResponseEntity.status(CONFLICT).body("Database error: " + ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<String>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        return just(ResponseEntity.status(FORBIDDEN).body("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        return just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<String>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException [status={}, reason={}]: {}", 
                ex.getStatusCode(), ex.getReason(), ex.getMessage(), ex);

        return just(ResponseEntity.status(ex.getStatusCode())
                .body(ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }
}

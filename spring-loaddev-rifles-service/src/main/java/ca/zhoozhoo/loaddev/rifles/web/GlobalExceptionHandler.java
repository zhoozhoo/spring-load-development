package ca.zhoozhoo.loaddev.rifles.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static reactor.core.publisher.Mono.just;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the rifles service.
 * <p>
 * This class provides centralized exception handling for all REST controllers in the
 * rifles service. It handles common exceptions like validation errors and number format
 * exceptions, returning appropriate HTTP status codes and error messages.
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

        var error = ex.getBindingResult()
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
}

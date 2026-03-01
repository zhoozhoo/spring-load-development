package ca.zhoozhoo.loaddev.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static reactor.test.StepVerifier.create;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/// Unit tests for [GlobalExceptionHandler].
/// Tests exception handling and response status codes for all exception handlers.
///
/// @author Zhubin Salehi
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNumberFormatException_shouldReturnBadRequest() {
        // given
        var exception = new NumberFormatException("Invalid number");

        // when & then
        create(handler.handleNumberFormatException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Invalid number format");
                    assertThat(response.getBody()).contains("Please provide valid numeric values");
                })
                .verifyComplete();
    }

    @Test
    void handleNumberFormatException_withSpecificMessage_shouldIncludeGenericMessage() {
        // given
        var exception = new NumberFormatException("For input string: \"abc\"");

        // when & then
        create(handler.handleNumberFormatException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isEqualTo("Invalid number format: Please provide valid numeric values");
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_shouldReturnBadRequest() {
        // given
        var exception = new ServerWebInputException("Required parameter missing");

        // when & then
        create(handler.handleServerWebInputException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Failed to read HTTP message");
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_withCause_shouldIncludeCauseMessage() {
        // given
        var cause = new IllegalArgumentException("Invalid argument");
        var exception = new ServerWebInputException("Bad input", null, cause);

        // when & then
        create(handler.handleServerWebInputException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Invalid argument");
                })
                .verifyComplete();
    }

    @Test
    void handleDecodingException_shouldReturnBadRequest() {
        // given
        var exception = new DecodingException("Failed to decode");

        // when & then
        create(handler.handleDecodingException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Failed to decode request body");
                })
                .verifyComplete();
    }

    @Test
    void handleDecodingException_withCause_shouldIncludeCauseMessage() {
        // given
        var cause = new RuntimeException("Unexpected token");
        var exception = new DecodingException("Failed to decode", cause);

        // when & then
        create(handler.handleDecodingException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Unexpected token");
                })
                .verifyComplete();
    }

    @Test
    void handleJacksonException_invalidFormat_shouldReturnBadRequest() {
        // given
        var exception = InvalidFormatException.from(null, "Cannot deserialize", "abc", Integer.class);

        // when & then
        create(handler.handleJacksonException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("JSON deserialization error");
                    assertThat(response.getBody()).contains("Invalid value: abc");
                    assertThat(response.getBody()).contains("expected type: Integer");
                })
                .verifyComplete();
    }

    @Test
    void handleJacksonException_mismatchedInput_shouldReturnBadRequest() {
        // given
        var exception = MismatchedInputException.from(null, Integer.class, "Expected integer");

        // when & then
        create(handler.handleJacksonException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("JSON deserialization error");
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withSingleError_shouldReturnBadRequest() throws NoSuchMethodException {
        // given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "rejected1", false, null, null, "Name is required"));
        
        var exception = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_withSingleError_shouldReturnBadRequest"), -1),
                bindingResult);
        
        // when & then
        create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("name");
                    assertThat(response.getBody()).contains("Name is required");
                    assertThat(response.getBody()).contains("rejected1");
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withMultipleErrors_shouldCombineMessages() throws NoSuchMethodException {
        // given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "", false, null, null, "Name is required"));
        bindingResult.addError(new FieldError("testObject", "weight", -1.0, false, null, null, "Weight must be positive"));
        bindingResult.addError(new FieldError("testObject", "length", -1, false, null, null, "Length must be positive"));
        
        var exception = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_withMultipleErrors_shouldCombineMessages"), -1),
                bindingResult);
        
        // when & then
        create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("name");
                    assertThat(response.getBody()).contains("Name is required");
                    assertThat(response.getBody()).contains("weight");
                    assertThat(response.getBody()).contains("Weight must be positive");
                    assertThat(response.getBody()).contains("length");
                    assertThat(response.getBody()).contains("Length must be positive");
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withNoErrors_shouldReturnDefaultMessage() throws NoSuchMethodException {
        // given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        var exception = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_withNoErrors_shouldReturnDefaultMessage"), -1),
                bindingResult);
        
        // when & then
        create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isEqualTo("Validation failed");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {
        // given
        var exception = new DataIntegrityViolationException("Constraint violation");

        // when & then
        create(handler.handleDataIntegrityViolation(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Database error");
                    assertThat(response.getBody()).contains("Constraint violation");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_withUniqueConstraint_shouldReturnConflict() {
        // given
        var exception = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [unique_name]");

        // when & then
        create(handler.handleDataIntegrityViolation(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).startsWith("Database error:");
                })
                .verifyComplete();
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        // given
        var exception = new AccessDeniedException("Access denied");

        // when & then
        create(handler.handleAccessDenied(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
                    assertThat(response.getBody()).isEqualTo("Access denied");
                })
                .verifyComplete();
    }

    @Test
    void handleAccessDenied_withCustomMessage_shouldReturnForbidden() {
        // given
        var exception = new AccessDeniedException("User does not have permission to access this resource");

        // when & then
        create(handler.handleAccessDenied(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
                    assertThat(response.getBody()).isEqualTo("Access denied");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        // given
        var exception = new RuntimeException("Unexpected error");

        // when & then
        create(handler.handleGenericException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_withNullPointerException_shouldReturnInternalServerError() {
        // given
        var exception = new NullPointerException("Cannot invoke method on null object");

        // when & then
        create(handler.handleGenericException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_withIllegalArgumentException_shouldReturnInternalServerError() {
        // given
        var exception = new IllegalArgumentException("Invalid argument provided");

        // when & then
        create(handler.handleGenericException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withReason_shouldReturnStatusAndReason() {
        // given
        var exception = new ResponseStatusException(NOT_FOUND, "Resource not found");

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
                    assertThat(response.getBody()).isEqualTo("Resource not found");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withoutReason_shouldReturnStatusAndMessage() {
        // given
        var exception = new ResponseStatusException(BAD_REQUEST);

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withUnauthorized_shouldReturnUnauthorized() {
        // given
        var exception = new ResponseStatusException(UNAUTHORIZED, "Authentication required");

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
                    assertThat(response.getBody()).isEqualTo("Authentication required");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withServiceUnavailable_shouldReturnServiceUnavailable() {
        // given
        var exception = new ResponseStatusException(
                SERVICE_UNAVAILABLE, 
                "Service temporarily unavailable");

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(SERVICE_UNAVAILABLE);
                    assertThat(response.getBody()).isEqualTo("Service temporarily unavailable");
                })
                .verifyComplete();
    }
}

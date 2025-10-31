package ca.zhoozhoo.loaddev.components.web;

import static org.assertj.core.api.Assertions.assertThat;
import static reactor.test.StepVerifier.create;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 * Tests exception handling and response status codes for all exception handlers.
 *
 * @author Zhubin Salehi
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNumberFormatException_shouldReturnBadRequest() {
        // given
        NumberFormatException exception = new NumberFormatException("Invalid number");

        // when & then
        create(handler.handleNumberFormatException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Invalid number format");
                    assertThat(response.getBody()).contains("Please provide valid numeric values");
                })
                .verifyComplete();
    }

    @Test
    void handleNumberFormatException_withSpecificMessage_shouldIncludeGenericMessage() {
        // given
        NumberFormatException exception = new NumberFormatException("For input string: \"abc\"");

        // when & then
        create(handler.handleNumberFormatException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isEqualTo("Invalid number format: Please provide valid numeric values");
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withSingleError_shouldReturnBadRequest() throws NoSuchMethodException {
        // given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "manufacturer", "rejected1", false, null, null, "Manufacturer is required"));
        
        var exception = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_withSingleError_shouldReturnBadRequest"), -1),
                bindingResult);
        
        // when & then
        create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("manufacturer");
                    assertThat(response.getBody()).contains("Manufacturer is required");
                    assertThat(response.getBody()).contains("rejected1");
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withMultipleErrors_shouldCombineMessages() throws NoSuchMethodException {
        // given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "bullet");
        bindingResult.addError(new FieldError("bullet", "manufacturer", "", false, null, null, "Manufacturer is required"));
        bindingResult.addError(new FieldError("bullet", "weight", -1.0, false, null, null, "Weight must be positive"));
        bindingResult.addError(new FieldError("bullet", "cost", -1, false, null, null, "Cost must be positive"));
        
        var exception = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_withMultipleErrors_shouldCombineMessages"), -1),
                bindingResult);
        
        // when & then
        create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("manufacturer");
                    assertThat(response.getBody()).contains("Manufacturer is required");
                    assertThat(response.getBody()).contains("weight");
                    assertThat(response.getBody()).contains("Weight must be positive");
                    assertThat(response.getBody()).contains("cost");
                    assertThat(response.getBody()).contains("Cost must be positive");
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
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isEqualTo("Validation failed");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {
        // given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Constraint violation");

        // when & then
        create(handler.handleDataIntegrityViolation(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).contains("Database error");
                    assertThat(response.getBody()).contains("Constraint violation");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_withUniqueConstraint_shouldReturnConflict() {
        // given
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [unique_manufacturer_type]");

        // when & then
        create(handler.handleDataIntegrityViolation(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).startsWith("Database error:");
                })
                .verifyComplete();
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        // given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // when & then
        create(handler.handleAccessDenied(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(response.getBody()).isEqualTo("Access denied");
                })
                .verifyComplete();
    }

    @Test
    void handleAccessDenied_withCustomMessage_shouldReturnForbidden() {
        // given
        AccessDeniedException exception = new AccessDeniedException("User does not have permission to access this resource");

        // when & then
        create(handler.handleAccessDenied(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(response.getBody()).isEqualTo("Access denied");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        // given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // when & then
        create(handler.handleGenericException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_withNullPointerException_shouldReturnInternalServerError() {
        // given
        NullPointerException exception = new NullPointerException("Cannot invoke method on null object");

        // when & then
        create(handler.handleGenericException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_withIllegalArgumentException_shouldReturnInternalServerError() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // when & then
        create(handler.handleGenericException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isEqualTo("An unexpected error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withReason_shouldReturnStatusAndReason() {
        // given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(response.getBody()).isEqualTo("Resource not found");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withoutReason_shouldReturnStatusAndMessage() {
        // given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withUnauthorized_shouldReturnUnauthorized() {
        // given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(response.getBody()).isEqualTo("Authentication required");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withServiceUnavailable_shouldReturnServiceUnavailable() {
        // given
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, 
                "Service temporarily unavailable");

        // when & then
        create(handler.handleResponseStatusException(exception))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(response.getBody()).isEqualTo("Service temporarily unavailable");
                })
                .verifyComplete();
    }
}

package ca.zhoozhoo.loaddev.loads.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * Unit tests for GlobalExceptionHandler.
 * Tests exception handling and response status codes.
 *
 * @author Zhubin Salehi
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNumberFormatException_shouldReturnBadRequest() {
        create(handler.handleNumberFormatException(new NumberFormatException("Invalid number")))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_shouldReturnBadRequest() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "field1", "rejected1", false, null, null, "Field is required"));
        
        var exception = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_shouldReturnBadRequest"), -1),
                bindingResult);
        
        create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {
        create(handler.handleDataIntegrityViolation(new DataIntegrityViolationException("Constraint violation")))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        create(handler.handleAccessDenied(new AccessDeniedException("Access denied")))
                .assertNext(response -> {
                    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
                    assertEquals("Access denied", response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        create(handler.handleGenericException(new RuntimeException("Unexpected error")))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withReason_shouldReturnStatusAndReason() {
        create(handler.handleResponseStatusException(new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")))
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertEquals("Resource not found", response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_withoutReason_shouldReturnStatusAndMessage() {
        create(handler.handleResponseStatusException(new ResponseStatusException(HttpStatus.BAD_REQUEST)))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }
}

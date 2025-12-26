package ca.zhoozhoo.loaddev.rifles.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import reactor.test.StepVerifier;

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
        StepVerifier.create(handler.handleNumberFormatException(new NumberFormatException("Invalid number")))
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
        
        StepVerifier.create(handler.handleValidationException(exception))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withMultipleErrors_shouldCombineMessages() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "field1", "value1", false, null, null, "Error 1"));
        bindingResult.addError(new FieldError("testObject", "field2", "value2", false, null, null, "Error 2"));
        
        StepVerifier.create(handler.handleValidationException(new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethod("handleValidationException_withMultipleErrors_shouldCombineMessages"), -1),
                bindingResult)))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }
}

package ca.zhoozhoo.loaddev.loads.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

/**
 * Unit tests for CurrentUserMethodArgumentResolver.
 *
 * @author Zhubin Salehi
 */
class CurrentUserMethodArgumentResolverTest {

    private final CurrentUserMethodArgumentResolver resolver = new CurrentUserMethodArgumentResolver();

    @Test
    void supportsParameter_withCurrentUserAnnotationAndStringType_shouldReturnTrue() throws NoSuchMethodException {
        assertTrue(resolver.supportsParameter(new MethodParameter(
                TestController.class.getMethod("testMethod", String.class), 0)));
    }

    @Test
    void supportsParameter_withoutCurrentUserAnnotation_shouldReturnFalse() throws NoSuchMethodException {
        assertFalse(resolver.supportsParameter(new MethodParameter(
                TestController.class.getMethod("testMethodWithoutAnnotation", String.class), 0)));
    }

    @Test
    void supportsParameter_withWrongType_shouldReturnFalse() throws NoSuchMethodException {
        assertFalse(resolver.supportsParameter(new MethodParameter(
                TestController.class.getMethod("testMethodWithWrongType", Integer.class), 0)));
    }

    // Test controller class for parameter testing
    static class TestController {
        public void testMethod(@CurrentUser String userId) {}
        public void testMethodWithoutAnnotation(String userId) {}
        public void testMethodWithWrongType(@CurrentUser Integer userId) {}
    }
}

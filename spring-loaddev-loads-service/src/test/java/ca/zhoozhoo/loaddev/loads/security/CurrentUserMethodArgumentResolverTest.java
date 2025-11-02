package ca.zhoozhoo.loaddev.loads.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.web.server.MockServerWebExchange.from;
import static reactor.test.StepVerifier.create;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.core.publisher.Mono;

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

    @Test
    void resolveArgument_withJwtPrincipal_shouldReturnUserId() throws NoSuchMethodException {
        create(resolver.resolveArgument(
                        new MethodParameter(TestController.class.getMethod("testMethod", String.class), 0),
                        null,
                        from(get("/").build())
                                .mutate()
                                .principal(Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                                        .header("alg", "none")
                                        .subject("user456")
                                        .build())))
                                .build()))
                .expectNext("user456")
                .verifyComplete();
    }

    @Test
    void resolveArgument_withNonJwtPrincipal_shouldReturnEmpty() throws NoSuchMethodException {
        create(resolver.resolveArgument(
                        new MethodParameter(TestController.class.getMethod("testMethod", String.class), 0),
                        null,
                        from(get("/").build())
                                .mutate()
                                .principal(Mono.just(new TestingAuthenticationToken("user", "password")))
                                .build()))
                .verifyComplete();
    }

    @Test
    void resolveArgument_withNoPrincipal_shouldReturnEmpty() throws NoSuchMethodException {
        create(resolver.resolveArgument(
                        new MethodParameter(TestController.class.getMethod("testMethod", String.class), 0),
                        null,
                        from(get("/").build())))
                .verifyComplete();
    }

    // Test controller class for parameter testing
    static class TestController {
        public void testMethod(@CurrentUser String userId) {}
        public void testMethodWithoutAnnotation(String userId) {}
        public void testMethodWithWrongType(@CurrentUser Integer userId) {}
    }
}

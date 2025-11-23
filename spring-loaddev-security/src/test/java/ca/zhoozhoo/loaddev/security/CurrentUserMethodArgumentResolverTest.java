package ca.zhoozhoo.loaddev.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import java.security.Principal;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CurrentUserMethodArgumentResolver}.
 * <p>
 * Verifies parameter support detection, JWT principal extraction, and handling of
 * non-JWT principals. Tests cover the resolver's behavior with various authentication
 * types and parameter configurations.
 *
 * @author Zhubin Salehi
 */
class CurrentUserMethodArgumentResolverTest {

    private final CurrentUserMethodArgumentResolver resolver = new CurrentUserMethodArgumentResolver();

    @SuppressWarnings("unused")
    void sample(@CurrentUser String userId, String other) {}

    @Test
    @DisplayName("supportsParameter true only for @CurrentUser String parameter")
    void supportsParameter() throws NoSuchMethodException {
        Method m = getClass().getDeclaredMethod("sample", String.class, String.class);
        MethodParameter p0 = new MethodParameter(m, 0);
        MethodParameter p1 = new MethodParameter(m, 1);
        assertThat(resolver.supportsParameter(p0)).isTrue();
        assertThat(resolver.supportsParameter(p1)).isFalse();
    }

    @Test
    @DisplayName("resolveArgument returns subject for Jwt principal")
    void resolveJwtPrincipal() throws NoSuchMethodException {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", "user123"));
        Authentication auth = new TestingAuthenticationToken(jwt, null, "ROLE_USER");
        auth.setAuthenticated(true);
        MockServerWebExchange base = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        var exchange = new ServerWebExchangeDecorator(base) {
            @Override
            public Mono<Principal> getPrincipal() {
                return Mono.just(auth);
            }
        };
        Method m = getClass().getDeclaredMethod("sample", String.class, String.class);
        MethodParameter p0 = new MethodParameter(m, 0);
        var result = resolver.resolveArgument(p0, null, exchange).block();
        assertThat(result).isEqualTo("user123");
    }

    @Test
    @DisplayName("resolveArgument returns null for non-Jwt principal")
    void resolveNonJwtPrincipal() throws NoSuchMethodException {
        Authentication auth = new TestingAuthenticationToken("plain", null);
        auth.setAuthenticated(true);
        MockServerWebExchange base = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        var exchange = new ServerWebExchangeDecorator(base) {
            @Override
            public Mono<Principal> getPrincipal() {
                return Mono.just(auth);
            }
        };
        Method m = getClass().getDeclaredMethod("sample", String.class, String.class);
        MethodParameter p0 = new MethodParameter(m, 0);
        var result = resolver.resolveArgument(p0, null, exchange).block();
        assertThat(result).isNull();
    }
}

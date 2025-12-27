package ca.zhoozhoo.loaddev.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ServerWebExchangeDecorator;

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
    void sample(@CurrentUser String userId, String other) {
    }

    @Test
    @DisplayName("supportsParameter true only for @CurrentUser String parameter")
    void supportsParameter() throws NoSuchMethodException {
        var mehod = getClass().getDeclaredMethod("sample", String.class, String.class);
        var paramether0 = new MethodParameter(mehod, 0);
        var paramether1 = new MethodParameter(mehod, 1);
        assertThat(resolver.supportsParameter(paramether0)).isTrue();
        assertThat(resolver.supportsParameter(paramether1)).isFalse();
    }

    @Test
    @DisplayName("resolveArgument returns subject for Jwt principal")
    void resolveJwtPrincipal() throws NoSuchMethodException {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", "user123"));
        var auth = new TestingAuthenticationToken(jwt, null, "ROLE_USER");
        auth.setAuthenticated(true);
        var base = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        var exchange = new ServerWebExchangeDecorator(base) {
            @Override
            public Mono<Principal> getPrincipal() {
                return Mono.just(auth);
            }
        };
        var mehod = getClass().getDeclaredMethod("sample", String.class, String.class);
        var paramether0 = new MethodParameter(mehod, 0);
        var result = resolver.resolveArgument(paramether0, null, exchange).block();
        assertThat(result).isEqualTo("user123");
    }

    @Test
    @DisplayName("resolveArgument returns null for non-Jwt principal")
    void resolveNonJwtPrincipal() throws NoSuchMethodException {
        var auth = new TestingAuthenticationToken("plain", null);
        auth.setAuthenticated(true);
        var exchange = new ServerWebExchangeDecorator(MockServerWebExchange.from(MockServerHttpRequest.get("/test").build())) {
            @Override
            public Mono<Principal> getPrincipal() {
                return Mono.just(auth);
            }
        };
        var method = getClass().getDeclaredMethod("sample", String.class, String.class);
        var parameter0 = new MethodParameter(method, 0);
        var result = resolver.resolveArgument(parameter0, null, exchange).block();
        assertThat(result).isNull();
    }
}

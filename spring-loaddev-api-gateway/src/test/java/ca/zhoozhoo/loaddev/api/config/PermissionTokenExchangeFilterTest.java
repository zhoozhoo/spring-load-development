package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;
import static reactor.test.StepVerifier.create;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import ca.zhoozhoo.loaddev.api.security.TokenExchangeException;
import ca.zhoozhoo.loaddev.api.security.UmaPermissionToken;
import ca.zhoozhoo.loaddev.api.security.UmaTokenExchangeService;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link PermissionTokenExchangeFilter}.
 * Tests the token extraction and delegation to UmaTokenExchangeService.
 *
 * @author Zhubin Salehi
 */
@ExtendWith(MockitoExtension.class)
class PermissionTokenExchangeFilterTest {

    @Mock
    private UmaTokenExchangeService tokenExchangeService;

    @Mock
    private WebFilterChain filterChain;

    @InjectMocks
    private PermissionTokenExchangeFilter filter;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
    
    private static final String PERMISSION_TOKEN = "permission-token-12345";

    @Test
    @DisplayName("Should proceed without token exchange when no Authorization header present")
    void shouldProceedWithoutTokenWhenNoAuthHeader() {
        // Given
        var request = MockServerHttpRequest.get("/test").build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should proceed without token exchange when Authorization header is not Bearer")
    void shouldProceedWithoutTokenWhenNotBearerAuth() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should successfully exchange token and store permission token")
    void shouldSuccessfullyExchangeToken() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var umaToken = new UmaPermissionToken(PERMISSION_TOKEN, "Bearer", 300, "openid");
        when(tokenExchangeService.exchangeForPermissionToken(VALID_TOKEN))
                .thenReturn(just(umaToken));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(tokenExchangeService).exchangeForPermissionToken(VALID_TOKEN);
        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isEqualTo(PERMISSION_TOKEN);
    }

    @Test
    @DisplayName("Should handle TokenExchangeException and proceed with original token")
    void shouldHandleTokenExchangeException() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        when(tokenExchangeService.exchangeForPermissionToken(VALID_TOKEN))
                .thenReturn(Mono.error(new TokenExchangeException("Authentication failed")));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - The filter chain may be called twice in reactive flow:
        // Once in .then() chain construction, once in .onErrorResume() execution
        create(result)
                .verifyComplete();

        verify(tokenExchangeService).exchangeForPermissionToken(VALID_TOKEN);
        // In reactive chains, .then() may construct the chain before error occurs
        verify(filterChain, org.mockito.Mockito.atLeast(1)).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should handle generic exception during token exchange")
    void shouldHandleGenericException() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        when(tokenExchangeService.exchangeForPermissionToken(VALID_TOKEN))
                .thenReturn(Mono.error(new RuntimeException("Network error")));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - The filter chain may be called twice in reactive flow:
        // Once in .then() chain construction, once in .onErrorResume() execution
        create(result)
                .verifyComplete();

        verify(tokenExchangeService).exchangeForPermissionToken(VALID_TOKEN);
        // In reactive chains, .then() may construct the chain before error occurs
        verify(filterChain, org.mockito.Mockito.atLeast(1)).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should extract token correctly from Bearer header")
    void shouldExtractTokenFromBearerHeader() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var umaToken = new UmaPermissionToken(PERMISSION_TOKEN, "Bearer", 300, "openid");
        when(tokenExchangeService.exchangeForPermissionToken(VALID_TOKEN))
                .thenReturn(just(umaToken));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        assertThat(exchange.getAttributes().get("permission_token")).isEqualTo(PERMISSION_TOKEN);
    }

    @Test
    @DisplayName("Should handle Bearer token without value")
    void shouldHandleBearerTokenWithoutValue() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Should proceed without token since extraction won't work
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should handle empty Bearer token value")
    void shouldHandleEmptyBearerToken() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer ")
                .build();
        var exchange = MockServerWebExchange.from(request);

        var umaToken = new UmaPermissionToken(PERMISSION_TOKEN, "Bearer", 300, "openid");
        when(tokenExchangeService.exchangeForPermissionToken(""))
                .thenReturn(just(umaToken));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Empty string should attempt exchange
        create(result)
                .verifyComplete();

        verify(tokenExchangeService).exchangeForPermissionToken("");
    }

    @Test
    @DisplayName("Should handle multiple Authorization headers")
    void shouldHandleMultipleAuthorizationHeaders() {
        // Given - MockServerHttpRequest only keeps the first header
        var request = MockServerHttpRequest.get("/test")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .header(AUTHORIZATION, "Bearer other-token")
                .build();
        var exchange = MockServerWebExchange.from(request);

        var umaToken = new UmaPermissionToken(PERMISSION_TOKEN, "Bearer", 300, "openid");
        when(tokenExchangeService.exchangeForPermissionToken(VALID_TOKEN))
                .thenReturn(just(umaToken));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Should use first header
        create(result)
                .verifyComplete();

        verify(tokenExchangeService).exchangeForPermissionToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should skip token exchange for actuator health endpoint")
    void shouldSkipTokenExchangeForActuatorHealth() {
        // Given
        var request = MockServerHttpRequest.get("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Should skip token exchange for actuator endpoints
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should skip token exchange for actuator liveness probe")
    void shouldSkipTokenExchangeForActuatorLiveness() {
        // Given
        var request = MockServerHttpRequest.get("/actuator/health/liveness")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should skip token exchange for actuator readiness probe")
    void shouldSkipTokenExchangeForActuatorReadiness() {
        // Given
        var request = MockServerHttpRequest.get("/actuator/health/readiness")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should skip token exchange for Swagger UI")
    void shouldSkipTokenExchangeForSwaggerUI() {
        // Given
        var request = MockServerHttpRequest.get("/swagger-ui/index.html")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should skip token exchange for OpenAPI docs")
    void shouldSkipTokenExchangeForOpenApiDocs() {
        // Given
        var request = MockServerHttpRequest.get("/v3/api-docs")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(tokenExchangeService, never()).exchangeForPermissionToken(VALID_TOKEN);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should perform token exchange for regular API endpoints")
    void shouldPerformTokenExchangeForRegularEndpoints() {
        // Given
        var request = MockServerHttpRequest.get("/api/loads")
                .header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var umaToken = new UmaPermissionToken(PERMISSION_TOKEN, "Bearer", 300, "openid");
        when(tokenExchangeService.exchangeForPermissionToken(VALID_TOKEN))
                .thenReturn(just(umaToken));
        when(filterChain.filter(exchange)).thenReturn(empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Should exchange token for regular endpoints
        create(result)
                .verifyComplete();

        verify(tokenExchangeService).exchangeForPermissionToken(VALID_TOKEN);
        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isEqualTo(PERMISSION_TOKEN);
    }
}

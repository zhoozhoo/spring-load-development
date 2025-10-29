package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static reactor.test.StepVerifier.create;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link TokenForwardingGatewayFilterFactory}.
 * Tests token forwarding logic in the gateway filter.
 * 
 * @author Zhubin Salehi
 */
class TokenForwardingGatewayFilterFactoryTest {

    private TokenForwardingGatewayFilterFactory filterFactory;
    private TokenForwardingGatewayFilterFactory.Config config;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        filterFactory = new TokenForwardingGatewayFilterFactory();
        config = new TokenForwardingGatewayFilterFactory.Config();
        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        attributes = new HashMap<>();

        when(exchange.getAttributes()).thenReturn(attributes);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should create filter factory instance")
    void shouldCreateFilterFactoryInstance() {
        assertThat(filterFactory)
                .isNotNull()
                .isInstanceOf(TokenForwardingGatewayFilterFactory.class);
    }

    @Test
    @DisplayName("Should create Config record instance")
    void shouldCreateConfigInstance() {
        assertThat(config)
                .isNotNull()
                .isInstanceOf(TokenForwardingGatewayFilterFactory.Config.class);
    }

    @Test
    @DisplayName("Should apply filter with empty config")
    void shouldApplyFilterWithEmptyConfig() {
        assertThat(filterFactory.apply(config))
                .isNotNull()
                .isInstanceOf(GatewayFilter.class);
    }

    @Test
    @DisplayName("Should forward request when no permission token is present")
    void shouldForwardRequestWhenNoPermissionToken() {
        create(filterFactory.apply(config).filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should add permission token to Authorization header when present")
    void shouldAddPermissionTokenToAuthorizationHeader() {
        var permissionToken = "permission-token-12345";
        attributes.put("permission_token", permissionToken);

        var request = mock(ServerHttpRequest.class);
        var requestBuilder = mock(ServerHttpRequest.Builder.class);
        var modifiedRequest = mock(ServerHttpRequest.class);
        var path = mock(org.springframework.http.server.RequestPath.class);
        var exchangeBuilder = mock(ServerWebExchange.Builder.class);
        var modifiedExchange = mock(ServerWebExchange.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(AUTHORIZATION, "Bearer %s".formatted(permissionToken)))
                .thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(modifiedRequest);
        when(modifiedRequest.getPath()).thenReturn(path);
        when(path.toString()).thenReturn("/test/path");
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(modifiedRequest)).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(modifiedExchange);
        when(chain.filter(modifiedExchange)).thenReturn(Mono.empty());

        create(filterFactory.apply(config).filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should use String.formatted() for Bearer token")
    void shouldUseFormattedForBearerToken() {
        assertThat("Bearer %s".formatted("test-token"))
                .isEqualTo("Bearer test-token");
    }

    @Test
    @DisplayName("Config record should be empty")
    void configRecordShouldBeEmpty() {
        var config1 = new TokenForwardingGatewayFilterFactory.Config();
        var config2 = new TokenForwardingGatewayFilterFactory.Config();
        
        assertThat(config1)
                .isEqualTo(config2)
                .hasSameHashCodeAs(config2);
        assertThat(config1.toString()).isEqualTo(config2.toString());
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullTokenGracefully() {
        attributes.put("permission_token", null);
        
        create(filterFactory.apply(config).filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyTokenGracefully() {
        attributes.put("permission_token", "");

        var request = mock(ServerHttpRequest.class);
        var requestBuilder = mock(ServerHttpRequest.Builder.class);
        var modifiedRequest = mock(ServerHttpRequest.class);
        var path = mock(org.springframework.http.server.RequestPath.class);
        var exchangeBuilder = mock(ServerWebExchange.Builder.class);
        var modifiedExchange = mock(ServerWebExchange.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(AUTHORIZATION, "Bearer "))
                .thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(modifiedRequest);
        when(modifiedRequest.getPath()).thenReturn(path);
        when(path.toString()).thenReturn("/test/path");
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(modifiedRequest)).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(modifiedExchange);
        when(chain.filter(modifiedExchange)).thenReturn(Mono.empty());

        create(filterFactory.apply(config).filter(exchange, chain))
                .verifyComplete();
    }
}

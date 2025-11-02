package ca.zhoozhoo.loaddev.mcp.config;

import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Test security configuration that provides a mock JWT authentication context.
 * <p>
 * This configuration is used in tests to bypass actual OAuth2 authentication
 * while still providing a valid JWT token in the reactive security context that
 * services can extract and use for authenticated requests.
 * <p>
 * The configuration uses a {@link WebFilter} to populate the reactive security
 * context with a mock {@link JwtAuthenticationToken} containing a JWT with
 * standard claims (sub, scope, iat, exp). This prevents service methods from
 * timing out when calling {@link ReactiveSecurityContextHolder#getContext()}.
 * <p>
 * Key features:
 * <ul>
 * <li>Disables CSRF protection for testing</li>
 * <li>Permits all requests without actual authentication</li>
 * <li>Injects mock JWT authentication at {@link SecurityWebFiltersOrder#AUTHENTICATION}</li>
 * <li>Uses {@code contextWrite()} to write authentication to reactive context</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see org.springframework.security.oauth2.jwt.Jwt
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
 * @see org.springframework.security.core.context.ReactiveSecurityContextHolder
 */
@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    /**
     * Creates a security filter chain that permits all requests and adds a filter
     * to populate the reactive security context with a mock JWT authentication.
     * <p>
     * Configuration details:
     * <ul>
     * <li>Disables CSRF protection since this is for testing only</li>
     * <li>Adds {@link #mockJwtAuthenticationFilter()} at AUTHENTICATION order</li>
     * <li>Permits all exchanges without requiring actual authentication</li>
     * </ul>
     * 
     * @param http the {@link ServerHttpSecurity} to configure
     * @return the configured {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
                .addFilterAt(mockJwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(auth -> auth.anyExchange().permitAll());
        return http.build();
    }

    /**
     * Creates a WebFilter that populates the reactive security context with a mock JWT.
     * <p>
     * This filter is critical for testing services that extract JWT tokens from
     * {@link ReactiveSecurityContextHolder}. Without this filter, calls to
     * {@code ReactiveSecurityContextHolder.getContext()} would never return,
     * causing service methods to timeout.
     * <p>
     * The filter creates a mock {@link Jwt} with:
     * <ul>
     * <li>Token value: "mock-token-value"</li>
     * <li>Algorithm header: "none"</li>
     * <li>Subject claim: "test-user"</li>
     * <li>Scope claim: "read write"</li>
     * <li>Valid timestamps (iat and exp)</li>
     * </ul>
     * <p>
     * The JWT is wrapped in a {@link JwtAuthenticationToken} and written to the
     * reactive context using {@code contextWrite()}, making it available to all
     * downstream reactive operators in service methods.
     * 
     * @return a {@link WebFilter} that injects mock JWT authentication
     */
    @Bean
    public WebFilter mockJwtAuthenticationFilter() {
        return new WebFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                // Create a mock JWT with required claims
                Jwt jwt = Jwt.withTokenValue("mock-token-value")
                        .header("alg", "none")
                        .claim("sub", "test-user")
                        .claim("scope", "read write")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build();

                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

                // Continue the filter chain with the mock authentication in the context
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
        };
    }
}

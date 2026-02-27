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

/// Test security configuration that provides a mock JWT authentication context.
///
/// This configuration is used in tests to bypass actual OAuth2 authentication
/// while still providing a valid JWT token in the reactive security context that
/// services can extract and use for authenticated requests.
///
/// The configuration uses a [WebFilter] to populate the reactive security
/// context with a mock [JwtAuthenticationToken] containing a JWT with
/// standard claims (sub, scope, iat, exp). This prevents service methods from
/// timing out when calling [ReactiveSecurityContextHolder#getContext()].
///
/// Key features:
///
/// - Disables CSRF protection for testing
/// - Permits all requests without actual authentication
/// - Injects mock JWT authentication at [SecurityWebFiltersOrder#AUTHENTICATION]
/// - Uses `contextWrite()` to write authentication to reactive context
///
/// @author Zhubin Salehi
/// @see org.springframework.security.oauth2.jwt.Jwt
/// @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
/// @see org.springframework.security.core.context.ReactiveSecurityContextHolder
@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    /// Creates a security filter chain that permits all requests and adds a filter
    /// to populate the reactive security context with a mock JWT authentication.
    ///
    /// Configuration details:
    ///
    /// - Disables CSRF protection since this is for testing only
    /// - Adds [#mockJwtAuthenticationFilter()] at AUTHENTICATION order
    /// - Permits all exchanges without requiring actual authentication
    ///
    /// @param http the [ServerHttpSecurity] to configure
    /// @return the configured [SecurityWebFilterChain]
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
                .addFilterAt(mockJwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(auth -> auth.anyExchange().permitAll());
        return http.build();
    }

    /// Creates a WebFilter that populates the reactive security context with a mock JWT.
    ///
    /// This filter is critical for testing services that extract JWT tokens from
    /// [ReactiveSecurityContextHolder]. Without this filter, calls to
    /// `ReactiveSecurityContextHolder.getContext()` would never return,
    /// causing service methods to timeout.
    ///
    /// The filter creates a mock [Jwt] with:
    ///
    /// - Token value: "mock-token-value"
    /// - Algorithm header: "none"
    /// - Subject claim: "test-user"
    /// - Scope claim: "read write"
    /// - Valid timestamps (iat and exp)
    ///
    /// The JWT is wrapped in a [JwtAuthenticationToken] and written to the
    /// reactive context using `contextWrite()`, making it available to all
    /// downstream reactive operators in service methods.
    ///
    /// @return a [WebFilter] that injects mock JWT authentication
    @Bean
    public WebFilter mockJwtAuthenticationFilter() {
        return new WebFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                // Create a mock JWT with required claims and wrap in authentication token
                var authentication = new JwtAuthenticationToken(
                    Jwt.withTokenValue("mock-token-value")
                        .header("alg", "none")
                        .claim("sub", "test-user")
                        .claim("scope", "read write")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());

                // Continue the filter chain with the mock authentication in the context
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
        };
    }
}

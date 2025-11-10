package ca.zhoozhoo.loaddev.loads.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Test security configuration for integration tests.
 * <p>
 * This configuration disables CSRF protection and permits all requests,
 * allowing tests to use mock JWT authentication via {@code @WithMockJwt}
 * without requiring actual OAuth2 authentication flows. This simplifies
 * testing while still allowing security annotations and authentication
 * injection to be tested.
 * </p>
 *
 * @author Zhubin Salehi
 */
@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    /**
     * Configures a permissive security filter chain for testing.
     * <p>
     * Disables CSRF and allows all requests to pass through without authentication,
     * enabling tests to focus on business logic while still supporting mock
     * authentication context when needed.
     * </p>
     *
     * @param http the ServerHttpSecurity to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable()).authorizeExchange(auth -> auth.anyExchange().permitAll());

        return http.build();
    }
}

package ca.zhoozhoo.loaddev.rifles.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Test security configuration for the rifles service.
 * <p>
 * Disables security for integration tests, allowing all requests without authentication.
 * This configuration is only active in test contexts and permits all exchanges without
 * requiring OAuth2 JWT tokens, enabling easier testing of business logic.
 * </p>
 *
 * @author Zhubin Salehi
 */
@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable()).authorizeExchange(auth -> auth.anyExchange().permitAll());

        return http.build();
    }
}

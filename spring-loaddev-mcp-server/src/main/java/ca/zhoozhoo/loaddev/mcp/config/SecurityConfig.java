package ca.zhoozhoo.loaddev.mcp.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.extern.log4j.Log4j2;

/**
 * Security configuration for the MCP server application.
 * Configures OAuth2 resource server with JWT authentication and defines security rules
 * for different URL patterns.
 * <p>
 * Key features:
 * <ul>
 * <li>OAuth2 resource server with JWT validation</li>
 * <li>Public access to actuator endpoints for monitoring</li>
 * <li>Protected access to MCP and SSE endpoints</li>
 * <li>Authentication required for all other endpoints</li>
 * </ul>
 * <p>
 * Note: This configuration is not active in test profile.
 * 
 * @author Zhubin Salehi
 */
@Configuration
@EnableWebFluxSecurity
@Profile("!test")
@Log4j2
public class SecurityConfig {

    /**
     * Configures the security filter chain for the application.
     * 
     * Security rules:
     * - /actuator/** - Public access for monitoring
     * - /mcp/**, /sse/** - Authenticated access for MCP operations
     * - All other paths require authentication
     *
     * @param http the ServerHttpSecurity to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.debug("Configuring SecurityWebFilterChain");
        
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/mcp/**", "/sse/**", "/sse").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }
}

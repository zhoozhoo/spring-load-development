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
 * Security configuration for OAuth2 resource server with JWT validation.
 * <p>
 * Public: /actuator/** | Authenticated: /mcp/**, /sse/** | All others: authenticated
 * <p>
 * Not active in test profile.
 * 
 * @author Zhubin Salehi
 */
@Configuration
@EnableWebFluxSecurity
@Profile("!test")
@Log4j2
public class SecurityConfig {

    /**
     * Configures security filter chain with public actuator access and authentication for other endpoints.
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

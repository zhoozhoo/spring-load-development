package ca.zhoozhoo.loaddev.loads.config;

import static java.util.UUID.randomUUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import ca.zhoozhoo.loaddev.loads.security.SecurityUtils;
import reactor.core.publisher.Mono;

@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
                .authorizeExchange(auth -> auth.anyExchange().permitAll());
        return http.build();
    }

    @Bean
    public SecurityUtils securityUtils() {
        return new SecurityUtils() {
            @Override
            public Mono<String> getCurrentUserId() {
                return Mono.just(randomUUID().toString());
            }
        };
    }
}

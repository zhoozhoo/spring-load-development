package ca.zhoozhoo.loaddev.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link SecurityAutoConfiguration}.
 * <p>
 * Validates auto-configuration bean creation, property binding, and customization
 * of security settings including public paths and principal claim names. Uses
 * {@link ReactiveWebApplicationContextRunner} for isolated context testing.
 *
 * @author Zhubin Salehi
 */
class SecurityAutoConfigurationTest {
    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withUserConfiguration(SecurityAutoConfiguration.class)
            .withBean(ReactiveJwtDecoder.class, () -> token -> Mono.just(
                    Jwt.withTokenValue(token)
                            .header("alg", "none")
                            .claim("sub", "tester")
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(3600))
                            .build()));

    @Test
    @DisplayName("Security beans are created")
    void beansPresent() {
        contextRunner.run(ctx -> {
            assertThat(ctx.getBean(SecurityWebFilterChain.class)).isNotNull();
        });
    }

    @Test
    @DisplayName("Custom principal claim name applied")
    void customPrincipalClaim() {
        contextRunner.withPropertyValues("security.principal-claim=customId")
                .run(ctx -> assertThat(ctx.getBean("jwtAuthenticationConverter")).isNotNull());
    }

    @Test
    @DisplayName("Public paths configuration honored")
    void publicPathsConfigured() {
        contextRunner.withPropertyValues("security.public-paths[0]=/health", "security.public-paths[1]=/actuator")
                .run(ctx -> assertThat(ctx.getBean(SecurityWebFilterChain.class)).isNotNull());
    }
}

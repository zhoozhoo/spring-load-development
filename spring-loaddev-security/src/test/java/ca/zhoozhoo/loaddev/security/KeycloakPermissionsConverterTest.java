package ca.zhoozhoo.loaddev.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Unit tests for {@link KeycloakPermissionsConverter}.
 * <p>
 * Verifies correct extraction of Keycloak permissions from JWT claims and
 * transformation into Spring Security {@code GrantedAuthority} instances.
 *
 * @author Zhubin Salehi
 */
class KeycloakPermissionsConverterTest {

    private final KeycloakPermissionsConverter converter = new KeycloakPermissionsConverter();

    @Test
    void convertsPermissionsWithScopes() {
        var jwt = Jwt.withTokenValue("test")
                .header("alg", "none")
                .claim("authorization", Map.of(
                        "permissions", List.of(
                                Map.of("rsname", "loads", "scopes", List.of("read", "write")),
                                Map.of("rsname", "rifles", "scopes", List.of("read")))))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var authorities = converter.convert(jwt);

        assertThat(authorities)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("loads:read", "loads:write", "rifles:read");
    }

    @Test
    void emptyWhenAuthorizationMissing() {
        var jwt = Jwt.withTokenValue("test").header("alg", "none")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        assertThat(converter.convert(jwt)).isEmpty();
    }
}

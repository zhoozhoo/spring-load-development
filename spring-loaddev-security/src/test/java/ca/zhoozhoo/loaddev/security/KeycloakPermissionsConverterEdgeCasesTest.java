package ca.zhoozhoo.loaddev.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.GrantedAuthority;

/**
 * Edge case tests for {@link KeycloakPermissionsConverter}.
 * <p>
 * Validates converter behavior with malformed claims, missing fields, null values,
 * and mixed data types in the Keycloak authorization structure. Ensures robust
 * handling of invalid or unexpected JWT claim formats.
 *
 * @author Zhubin Salehi
 */
class KeycloakPermissionsConverterEdgeCasesTest {

    private final KeycloakPermissionsConverter converter = new KeycloakPermissionsConverter();

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), claims);
    }

    @Test
    @DisplayName("Returns empty list when authorization claim missing")
    void noAuthorizationClaim() {
        var result = converter.convert(jwt(Map.of("sub", "user1")));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when permissions not a List")
    void permissionsNotAList() {
        var auth = Map.of("permissions", "not-a-list");
        var result = converter.convert(jwt(Map.of("authorization", auth)));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns empty when permission map lacks rsname")
    void permissionMissingRsname() {
        var perm = Map.of("scopes", List.of("edit"));
        var auth = Map.of("permissions", List.of(perm));
        var result = converter.convert(jwt(Map.of("authorization", auth)));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns authorities when scopes present and strings")
    void scopesProduceAuthorities() {
        var perm = Map.of("rsname", "loads", "scopes", List.of("edit", "view"));
        var auth = Map.of("permissions", List.of(perm));
        var result = converter.convert(jwt(Map.of("authorization", auth)));
        assertThat(result).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("loads:edit", "loads:view");
    }

    @Test
    @DisplayName("Skip non-string scopes and only map valid ones")
    void mixedScopeTypes() {
        var perm = Map.of("rsname", "groups", "scopes", List.of("list", 42, new Object(), "delete"));
        var auth = Map.of("permissions", List.of(perm));
        var result = converter.convert(jwt(Map.of("authorization", auth)));
        assertThat(result).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("groups:list", "groups:delete");
    }

    @Test
    @DisplayName("Empty when scopes key present but null")
    void nullScopes() {
        var perm = new HashMap<String, Object>();
        perm.put("rsname", "shots");
        perm.put("scopes", null); // explicitly null to exercise branch
        var auth = Map.of("permissions", List.of(perm));
        var result = converter.convert(jwt(Map.of("authorization", auth)));
        assertThat(result).isEmpty();
    }
}

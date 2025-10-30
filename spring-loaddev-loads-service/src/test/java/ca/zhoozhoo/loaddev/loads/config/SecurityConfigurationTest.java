package ca.zhoozhoo.loaddev.loads.config;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import ca.zhoozhoo.loaddev.loads.config.SecurityConfiguration.KeycloakPermissionsConverter;

/**
 * Unit tests for SecurityConfiguration.KeycloakPermissionsConverter.
 * Tests extraction of permissions from Keycloak JWT tokens.
 *
 * @author Zhubin Salehi
 */
class SecurityConfigurationTest {

    private final KeycloakPermissionsConverter converter = new KeycloakPermissionsConverter();

    @Test
    void convert_withValidPermissions_shouldExtractAuthorities() {
        var permissions = List.of(
                Map.of(
                        "rsname", "resource1",
                        "scopes", List.of("read", "write")),
                Map.of(
                        "rsname", "resource2",
                        "scopes", List.of("delete")));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", permissions))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(3, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:write")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource2:delete")));
    }

    @Test
    void convert_withNoAuthorizationClaim_shouldReturnEmptyList() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user123")
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withNullPermissions_shouldReturnEmptyList() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", "not-a-list"))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withEmptyPermissions_shouldReturnEmptyList() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", emptyList()))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withPermissionMissingRsname_shouldSkipPermission() {
        var permissions = List.of(
                Map.of("scopes", List.of("read")),
                Map.of("rsname", "resource1", "scopes", List.of("write")));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", permissions))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("resource1:write", authorities.iterator().next().getAuthority());
    }

    @Test
    void convert_withNullScopes_shouldSkipPermission() {
        var permissions = List.of(
                Map.of("rsname", "resource1"));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", permissions))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withNonListScopes_shouldSkipPermission() {
        var permissions = List.of(
                Map.of("rsname", "resource1", "scopes", "not-a-list"));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", permissions))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withNonStringScope_shouldSkipScope() {
        var permissions = List.of(
                Map.of("rsname", "resource1", "scopes", List.of("read", 123, "write")));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", permissions))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:write")));
    }

    @Test
    void convert_withNonMapPermission_shouldSkipPermission() {
        var permissions = List.of(
                "not-a-map",
                Map.of("rsname", "resource1", "scopes", List.of("read")));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", permissions))
                .build();

        var authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("resource1:read", authorities.iterator().next().getAuthority());
    }
}

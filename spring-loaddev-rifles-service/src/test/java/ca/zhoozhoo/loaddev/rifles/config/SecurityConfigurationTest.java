package ca.zhoozhoo.loaddev.rifles.config;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.oauth2.jwt.Jwt.withTokenValue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.zhoozhoo.loaddev.rifles.config.SecurityConfiguration.KeycloakPermissionsConverter;

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
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", List.of(
                        Map.of(
                                "rsname", "resource1",
                                "scopes", List.of("read", "write")),
                        Map.of(
                                "rsname", "resource2",
                                "scopes", List.of("delete")))))
                .build());

        assertNotNull(authorities);
        assertEquals(3, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:write")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource2:delete")));
    }

    @Test
    void convert_withNoAuthorizationClaim_shouldReturnEmptyList() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user123")
                .build());

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withNullPermissions_shouldReturnEmptyList() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", "not-a-list"))
                .build());

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withEmptyPermissions_shouldReturnEmptyList() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", emptyList()))
                .build());

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withPermissionMissingRsname_shouldSkipPermission() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", List.of(
                        Map.of("scopes", List.of("read")),
                        Map.of("rsname", "resource1", "scopes", List.of("write")))))
                .build());

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("resource1:write", authorities.iterator().next().getAuthority());
    }

    @Test
    void convert_withNullScopes_shouldSkipPermission() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", List.of(
                        Map.of("rsname", "resource1"))))
                .build());

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withNonListScopes_shouldSkipPermission() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", List.of(
                        Map.of("rsname", "resource1", "scopes", "not-a-list"))))
                .build());

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void convert_withNonStringScope_shouldSkipScope() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", List.of(
                        Map.of("rsname", "resource1", "scopes", List.of("read", 123, "write")))))
                .build());

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("resource1:write")));
    }

    @Test
    void convert_withNonMapPermission_shouldSkipPermission() {
        var authorities = converter.convert(withTokenValue("token")
                .header("alg", "none")
                .claim("authorization", Map.of("permissions", List.of(
                        "not-a-map",
                        Map.of("rsname", "resource1", "scopes", List.of("read")))))
                .build());

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("resource1:read", authorities.iterator().next().getAuthority());
    }
}

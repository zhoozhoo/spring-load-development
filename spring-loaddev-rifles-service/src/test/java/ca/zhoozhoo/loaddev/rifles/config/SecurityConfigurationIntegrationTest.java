package ca.zhoozhoo.loaddev.rifles.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Unit tests for SecurityConfiguration components.
 * Tests JWT authentication conversion with Keycloak permissions.
 *
 * @author Zhubin Salehi
 */
class SecurityConfigurationIntegrationTest {

    private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

    @Test
    void jwtAuthenticationConverter_shouldBeConfigured() {
        assertNotNull(securityConfiguration.jwtAuthenticationConverter(), "JWT converter should not be null");
    }

    @Test
    void jwtAuthenticationConverter_shouldConvertJwtWithPermissions() {
        var result = securityConfiguration.jwtAuthenticationConverter()
                .convert(Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .subject("user123")
                        .claim("authorization", Map.of("permissions", List.of(
                                Map.of("rsname", "resource1", "scopes", List.of("read", "write")))))
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build())
                .block();
        
        assertNotNull(result, "Authentication should not be null");
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("resource1:read")));
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("resource1:write")));
    }

    @Test
    void jwtAuthenticationConverter_shouldConvertJwtWithMultipleResources() {
        var result = securityConfiguration.jwtAuthenticationConverter()
                .convert(Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .subject("user123")
                        .claim("authorization", Map.of("permissions", List.of(
                                Map.of("rsname", "rifles", "scopes", List.of("read")),
                                Map.of("rsname", "loads", "scopes", List.of("write")))))
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build())
                .block();
        
        assertNotNull(result, "Authentication should not be null");
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("rifles:read")));
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("loads:write")));
    }

    @Test
    void jwtAuthenticationConverter_shouldHandleEmptyPermissions() {
        assertNotNull(securityConfiguration.jwtAuthenticationConverter()
                .convert(Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .subject("user123")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build())
                .block(),
                "Authentication should not be null");
    }

    @Test
    void keycloakPermissionsConverter_shouldExtractAuthorities() {
        var authorities = new SecurityConfiguration.KeycloakPermissionsConverter()
                .convert(Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .subject("user123")
                        .claim("authorization", Map.of("permissions", List.of(
                                Map.of("rsname", "resource1", "scopes", List.of("read", "write")))))
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());
        
        assertNotNull(authorities);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("resource1:read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("resource1:write")));
    }
}

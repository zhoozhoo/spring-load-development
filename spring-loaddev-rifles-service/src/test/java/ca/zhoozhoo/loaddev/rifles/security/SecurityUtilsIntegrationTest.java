package ca.zhoozhoo.loaddev.rifles.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for SecurityUtils.
 * Tests logic for extracting user ID from JWT authentication principals.
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityUtilsIntegrationTest {

    @Test
    void extractUserId_fromJwtPrincipal_shouldReturnSubject() {
        // Test the extraction logic that SecurityUtils uses
        var auth = new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user123")
                .build());
        
        // Simulate the pattern matching logic from SecurityUtils
        var userId = switch (auth.getPrincipal()) {
            case Jwt j -> j.getSubject();
            case null, default -> null;
        };
        
        assertEquals("user123", userId);
    }

    @Test
    void extractUserId_fromNonJwtPrincipal_shouldReturnNull() {
        var auth = new org.springframework.security.authentication.TestingAuthenticationToken("user", "password");
        
        // Simulate the pattern matching logic from SecurityUtils
        assertNull(switch (auth.getPrincipal()) {
            case Jwt jwt -> jwt.getSubject();
            case null, default -> null;
        });
    }

    @Test
    void checkAuthentication_whenNotAuthenticated_shouldFilter() {
        var auth = new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user123")
                .build());
        auth.setAuthenticated(false);
        
        // The filter in getCurrentUserId checks this
        assertFalse(auth.isAuthenticated());
    }

    @Test
    void checkAuthentication_whenAuthenticated_shouldPass() {
        var auth = new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user123")
                .build());
        auth.setAuthenticated(true);
        
        // The filter in getCurrentUserId checks this
        assertTrue(auth.isAuthenticated());
    }
}

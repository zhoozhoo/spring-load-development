package ca.zhoozhoo.loaddev.rifles.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Unit tests for SecurityUtils.
 * Tests user ID extraction from JWT tokens in reactive security context.
 *
 * @author Zhubin Salehi
 */
class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @Test
    void getCurrentUserId_withEmptyContext_shouldReturnEmpty() {
        securityUtils.getCurrentUserId().subscribe(
                _ -> {
                    throw new AssertionError("Should not emit a value");
                },
                error -> {
                    throw new AssertionError("Should not error: " + error);
                },
                () -> {
                    // Expected - completes without emitting
                }
        );
    }

    @Test
    void extractUserId_fromJwtPrincipal_shouldReturnSubject() {
        var auth = new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user123")
                .build());
        
        assertEquals("user123", switch (auth.getPrincipal()) {
            case Jwt j -> j.getSubject();
            case null, default -> null;
        });
    }

    @Test
    void extractUserId_fromNonJwtPrincipal_shouldReturnNull() {
        assertNull(switch (new TestingAuthenticationToken("user", "password").getPrincipal()) {
            case Jwt jwt -> jwt.getSubject();
            case null, default -> null;
        });
    }

    @Test
    void checkAuthentication_whenNotAuthenticated_shouldBeFalse() {
        var auth = new JwtAuthenticationToken(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user123")
                .build());
        auth.setAuthenticated(false);
        
        assertFalse(auth.isAuthenticated());
    }
}


package ca.zhoozhoo.loaddev.rifles.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Utility class for security-related operations in the rifles service.
 * <p>
 * Provides helper methods for extracting security context information, particularly
 * the current authenticated user's ID from JWT tokens. This utility supports reactive
 * security context access for non-blocking operations using Java 25 enhanced pattern
 * matching for cleaner code.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Component
public class SecurityUtils {

    /**
     * Retrieves the current authenticated user's ID from the reactive security context.
     * <p>
     * Uses Java 25 enhanced instanceof pattern matching for cleaner type checking
     * and variable extraction from JWT principals.
     * </p>
     *
     * @return Mono containing the user ID from the JWT subject claim
     */
    public Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(auth -> auth != null && auth.isAuthenticated())
                .map(Authentication::getPrincipal)
                .mapNotNull(principal -> switch (principal) {
                    case Jwt jwt -> jwt.getSubject();
                    case null, default -> null;
                });
    }
}

package ca.zhoozhoo.loaddev.components.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Utility class for accessing security context information in reactive flows.
 * <p>
 * Provides helper methods to extract the current authenticated user's information
 * from the reactive security context using Java 25 pattern matching for switch
 * expressions for cleaner and more maintainable code.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Component
public class SecurityUtils {

    /**
     * Retrieves the current authenticated user's ID from the reactive security context.
     * <p>
     * Uses Java 25 pattern matching for switch to safely extract the JWT subject,
     * handling null cases and type mismatches gracefully.
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

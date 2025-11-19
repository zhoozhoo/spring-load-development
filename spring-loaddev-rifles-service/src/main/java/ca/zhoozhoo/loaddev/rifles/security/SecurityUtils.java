package ca.zhoozhoo.loaddev.rifles.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Security utility for extracting user information from JWT tokens.
 * <p>
 * Provides reactive access to security context for non-blocking operations.
 *
 * @author Zhubin Salehi
 */
@Component
public class SecurityUtils {

    /**
     * Retrieves the current authenticated user's ID from JWT subject claim.
     *
     * @return Mono containing the user ID
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

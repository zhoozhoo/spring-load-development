package ca.zhoozhoo.loaddev.components.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * Reactive security utilities using Java 25 pattern matching.
 *
 * @author Zhubin Salehi
 */
@Component
public class SecurityUtils {

    /**
     * Authenticated user's ID from JWT subject.
     *
     * @return Mono containing user ID
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

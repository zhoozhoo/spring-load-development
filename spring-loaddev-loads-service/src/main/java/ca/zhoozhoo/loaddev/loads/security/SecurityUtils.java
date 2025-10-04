package ca.zhoozhoo.loaddev.loads.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * Utility class for accessing security context information in reactive flows.
 * <p>
 * Provides helper methods to extract the current authenticated user's information
 * from the reactive security context. Useful in service layer methods where the
 * {@link CurrentUser} annotation cannot be used directly.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Component
public class SecurityUtils {

    public Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(auth -> auth != null && auth.isAuthenticated())
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(jwt -> jwt.getSubject());
    }
}

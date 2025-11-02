package ca.zhoozhoo.loaddev.components.security;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Custom method argument resolver for extracting the current user's ID from JWT tokens.
 * <p>
 * This resolver intercepts controller method parameters annotated with {@link CurrentUser}
 * and automatically injects the authenticated user's ID (extracted from the JWT subject claim).
 * It enables clean and declarative access to the current user context in reactive controllers.
 * Uses Java 25 pattern matching for switch to safely handle different principal types.
 * </p>
 *
 * @author Zhubin Salehi
 * @see CurrentUser
 */
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public @NonNull Mono<Object> resolveArgument(@NonNull MethodParameter parameter,
            @NonNull BindingContext bindingContext,
            @NonNull ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(Authentication::getPrincipal)
                .mapNotNull(principal -> switch (principal) {
                    case Jwt jwt -> jwt.getSubject();
                    case null, default -> null;
                });
    }
}

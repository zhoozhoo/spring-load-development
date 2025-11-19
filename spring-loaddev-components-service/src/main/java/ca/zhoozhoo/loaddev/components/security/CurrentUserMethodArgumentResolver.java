package ca.zhoozhoo.loaddev.components.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Resolves CurrentUser parameters using Java 25 pattern matching.
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

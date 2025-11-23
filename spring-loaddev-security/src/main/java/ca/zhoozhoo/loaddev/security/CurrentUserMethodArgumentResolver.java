package ca.zhoozhoo.loaddev.security;

import org.springframework.core.MethodParameter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Argument resolver for {@link CurrentUser} annotated method parameters.
 * <p>
 * Extracts the JWT subject claim from the security context and injects it into
 * controller method parameters annotated with {@code @CurrentUser}. Only supports
 * {@code String} type parameters. Uses Java 25 pattern matching for type-safe
 * principal handling.
 * <p>
 * Automatically registered when {@link SecurityAutoConfiguration} is active.
 *
 * @author Zhubin Salehi
 * @see CurrentUser
 * @see HandlerMethodArgumentResolver
 */
@Component
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

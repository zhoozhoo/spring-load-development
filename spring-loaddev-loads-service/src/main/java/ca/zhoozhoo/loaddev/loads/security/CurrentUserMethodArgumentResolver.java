package ca.zhoozhoo.loaddev.loads.security;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

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
                .cast(Jwt.class)
                .map(Jwt::getSubject);
    }
}

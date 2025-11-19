package ca.zhoozhoo.loaddev.rifles.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Resolves {@link CurrentUser} annotated parameters by extracting user ID from JWT tokens.
 * <p>
 * Extracts the subject claim from authenticated JWT tokens, enabling clean controller code
 * without manual authentication extraction.
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
        // Obtain Authentication from the exchange principal if present, otherwise
        // fall back to the ReactiveSecurityContextHolder (used by mockJwt()).
        Mono<Authentication> authenticationMono = exchange.getPrincipal()
                .cast(Authentication.class)
                .switchIfEmpty(
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .filter(auth -> auth != null));

        return authenticationMono.flatMap(auth -> {
            if (auth == null) {
                return Mono.empty();
            }
            Object principal = auth.getPrincipal();
            // Only resolve for JWT principals; any other principal should yield empty
            if (principal instanceof Jwt jwt) {
                String subject = jwt.getSubject();
                if (subject == null || subject.isBlank()) {
                    subject = jwt.getClaimAsString("sub");
                }
                return Mono.justOrEmpty(subject);
            }
            return Mono.empty();
        });
    }
}

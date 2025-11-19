package ca.zhoozhoo.loaddev.loads.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects current authenticated user's ID from JWT token.
 * <p>
 * Example: {@code public Mono<Load> getLoad(@CurrentUser String userId) { ... }}
 *
 * @author Zhubin Salehi
 * @see CurrentUserMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}

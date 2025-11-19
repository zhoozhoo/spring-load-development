package ca.zhoozhoo.loaddev.rifles.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the current authenticated user's ID from JWT token.
 * <p>
 * Example:
 * <pre>
 * public Mono&lt;ResponseEntity&lt;Rifle&gt;&gt; getRifle(@CurrentUser String userId) {
 *     // userId automatically extracted from JWT subject claim
 * }
 * </pre>
 *
 * @author Zhubin Salehi
 * @see CurrentUserMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}

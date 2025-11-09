package ca.zhoozhoo.loaddev.components.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter-level annotation for injecting the current authenticated user's ID.
 * <p>
 * When applied to a controller method parameter of type String, this annotation
 * triggers the {@link CurrentUserMethodArgumentResolver} to extract and inject
 * the user ID (subject claim) from the JWT token of the authenticated user.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * public Mono&lt;ResponseEntity&lt;Projectile&gt;&gt; getProjectile(@CurrentUser String userId) {
 *     // userId is automatically extracted from JWT
 * }
 * </pre>
 * </p>
 *
 * @author Zhubin Salehi
 * @see CurrentUserMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}

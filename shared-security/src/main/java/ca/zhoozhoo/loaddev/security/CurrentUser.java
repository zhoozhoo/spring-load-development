package ca.zhoozhoo.loaddev.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotation for injecting the authenticated user's ID into controller method parameters.
///
/// Extracts the JWT subject claim by default. Use on `String` parameters in reactive
/// WebFlux controllers to automatically resolve the current user's ID from the security context.
///
/// Example usage:
/// ```
/// public Mono&lt;ResponseEntity&lt;Load&gt;&gt; getLoad(@CurrentUser String userId, @PathVariable Long id) {
///     return loadService.findByIdAndUserId(id, userId)
///         .map(ResponseEntity::ok);
/// }
/// ```
///
/// @author Zhubin Salehi
/// @see CurrentUserMethodArgumentResolver
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}

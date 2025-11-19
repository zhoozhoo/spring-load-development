package ca.zhoozhoo.loaddev.components.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects authenticated user's ID from JWT subject claim.
 *
 * @author Zhubin Salehi
 * @see CurrentUserMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}

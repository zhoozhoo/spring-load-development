package ca.zhoozhoo.loaddev.loads.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom validation constraint for Load measurement requirements.
 * <p>
 * This annotation ensures that a Load entity has at least one cartridge measurement
 * specified: either distance from lands or case overall length. It enforces business
 * rules at the validation layer to ensure data integrity for load configurations.
 * </p>
 * <p>
 * The actual validation logic is implemented in {@link LoadMeasurementValidator}.
 * </p>
 *
 * @author Zhubin Salehi
 * @see LoadMeasurementValidator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoadMeasurementValidator.class)
@Documented
public @interface LoadMeasurement {
    String message() default "Either distance from lands or case overall length must be specified";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

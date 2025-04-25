package ca.zhoozhoo.loaddev.loads.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoadMeasurementValidator.class)
@Documented
public @interface LoadMeasurement {
    String message() default "Either distance from lands or case overall length must be specified";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

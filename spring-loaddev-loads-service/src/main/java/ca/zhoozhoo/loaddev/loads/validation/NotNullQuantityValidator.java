package ca.zhoozhoo.loaddev.loads.validation;

import javax.measure.Quantity;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

/**
 * Custom validator for @NotNull constraint on JSR-385 Quantity types.
 * <p>
 * Validates that a Quantity is not null.
 * This validator is needed because Bean Validation needs explicit validators
 * for generic types like Quantity.
 * </p>
 *
 * @author Zhubin Salehi
 */
public class NotNullQuantityValidator implements ConstraintValidator<NotNull, Quantity<?>> {

    @Override
    public void initialize(NotNull constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Quantity<?> value, ConstraintValidatorContext context) {
        return value != null;
    }
}

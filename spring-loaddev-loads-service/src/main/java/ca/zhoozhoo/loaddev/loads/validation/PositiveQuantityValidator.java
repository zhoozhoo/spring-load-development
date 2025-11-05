package ca.zhoozhoo.loaddev.loads.validation;

import javax.measure.Quantity;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Positive;

/**
 * Custom validator for @Positive constraint on JSR-385 Quantity types.
 * <p>
 * Validates that a Quantity value is positive (greater than zero).
 * Returns true for null values as @NotNull should be used for null checking.
 * </p>
 *
 * @author Zhubin Salehi
 */
public class PositiveQuantityValidator implements ConstraintValidator<Positive, Quantity<?>> {

    @Override
    public void initialize(Positive constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Quantity<?> value, ConstraintValidatorContext context) {
        // null values are valid - use @NotNull for null checking
        if (value == null) {
            return true;
        }

        // Check if the quantity value is positive
        return value.getValue().doubleValue() > 0.0;
    }
}

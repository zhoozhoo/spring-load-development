package ca.zhoozhoo.loaddev.loads.validation;

import ca.zhoozhoo.loaddev.loads.model.Load;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the {@link LoadMeasurement} constraint.
 * <p>
 * This validator ensures that a Load entity has at least one cartridge measurement
 * specified (either distanceFromLands or caseOverallLength). If neither field is
 * populated, it adds constraint violations to both fields with appropriate error messages.
 * </p>
 *
 * @author Zhubin Salehi
 * @see LoadMeasurement
 */
public class LoadMeasurementValidator implements ConstraintValidator<LoadMeasurement, Load> {
    
    @Override
    public boolean isValid(Load load, ConstraintValidatorContext context) {
        if (load == null) {
            return true;
        }
        boolean valid = load.distanceFromLands() != null || load.caseOverallLength() != null;
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Either distance from lands or case overall length must be specified")
                .addPropertyNode("distanceFromLands")
                .addConstraintViolation();
            context.buildConstraintViolationWithTemplate("Either distance from lands or case overall length must be specified")
                .addPropertyNode("caseOverallLength")
                .addConstraintViolation();
        }
        return valid;
    }
}

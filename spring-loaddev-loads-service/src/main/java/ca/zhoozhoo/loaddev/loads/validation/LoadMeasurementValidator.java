package ca.zhoozhoo.loaddev.loads.validation;

import ca.zhoozhoo.loaddev.loads.model.Load;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

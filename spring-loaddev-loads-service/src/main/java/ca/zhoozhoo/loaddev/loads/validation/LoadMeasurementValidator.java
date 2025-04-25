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
        return load.distanceFromLands() != null || load.caseOverallLength() != null;
    }
}

package ca.zhoozhoo.loaddev.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

/**
 * Unit tests for NotNullQuantityValidator.
 *
 * @author Zhubin Salehi
 */
@DisplayName("NotNullQuantityValidator Tests")
class NotNullQuantityValidatorTest {

    private NotNullQuantityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NotNullQuantityValidator();
        validator.initialize(null); // initialize method doesn't use the annotation
    }

    @Test
    @DisplayName("Should return true for non-null Quantity")
    void shouldReturnTrueForNonNullQuantity() {
        Quantity<Mass> mass = Quantities.getQuantity(150, Units.GRAM);
        
        boolean result = validator.isValid(mass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for null Quantity")
    void shouldReturnFalseForNullQuantity() {
        boolean result = validator.isValid(null, null);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true for zero value Quantity")
    void shouldReturnTrueForZeroValueQuantity() {
        Quantity<Mass> zeroMass = Quantities.getQuantity(0, Units.GRAM);
        
        boolean result = validator.isValid(zeroMass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for negative value Quantity")
    void shouldReturnTrueForNegativeValueQuantity() {
        Quantity<Length> negativeLength = Quantities.getQuantity(-10, Units.METRE);
        
        boolean result = validator.isValid(negativeLength, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for very small positive Quantity")
    void shouldReturnTrueForVerySmallPositiveQuantity() {
        Quantity<Mass> tinyMass = Quantities.getQuantity(0.0001, Units.GRAM);
        
        boolean result = validator.isValid(tinyMass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for very large Quantity")
    void shouldReturnTrueForVeryLargeQuantity() {
        Quantity<Mass> largeMass = Quantities.getQuantity(999999999.99, Units.KILOGRAM);
        
        boolean result = validator.isValid(largeMass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle initialize with NotNull annotation")
    void shouldHandleInitializeWithNotNullAnnotation() {
        // Create a mock NotNull annotation (we pass null since initialize doesn't use it)
        validator.initialize(null);
        
        Quantity<Mass> mass = Quantities.getQuantity(100, Units.GRAM);
        boolean result = validator.isValid(mass, null);
        
        assertTrue(result);
    }
}

package ca.zhoozhoo.loaddev.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

/**
 * Unit tests for PositiveQuantityValidator.
 *
 * @author Zhubin Salehi
 */
@DisplayName("PositiveQuantityValidator Tests")
class PositiveQuantityValidatorTest {

    private PositiveQuantityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PositiveQuantityValidator();
        validator.initialize(null); // initialize method doesn't use the annotation
    }

    @Test
    @DisplayName("Should return true for positive Quantity")
    void shouldReturnTrueForPositiveQuantity() {
        Quantity<Mass> mass = Quantities.getQuantity(150, Units.GRAM);
        
        boolean result = validator.isValid(mass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for null Quantity")
    void shouldReturnTrueForNullQuantity() {
        boolean result = validator.isValid(null, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for zero value Quantity")
    void shouldReturnFalseForZeroValueQuantity() {
        Quantity<Mass> zeroMass = Quantities.getQuantity(0, Units.GRAM);
        
        boolean result = validator.isValid(zeroMass, null);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for negative value Quantity")
    void shouldReturnFalseForNegativeValueQuantity() {
        Quantity<Length> negativeLength = Quantities.getQuantity(-10, Units.METRE);
        
        boolean result = validator.isValid(negativeLength, null);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true for very small positive Quantity")
    void shouldReturnTrueForVerySmallPositiveQuantity() {
        Quantity<Mass> tinyMass = Quantities.getQuantity(0.0001, Units.GRAM);
        
        boolean result = validator.isValid(tinyMass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for very large positive Quantity")
    void shouldReturnTrueForVeryLargePositiveQuantity() {
        Quantity<Mass> largeMass = Quantities.getQuantity(999999999.99, Units.KILOGRAM);
        
        boolean result = validator.isValid(largeMass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for negative decimal Quantity")
    void shouldReturnFalseForNegativeDecimalQuantity() {
        Quantity<Speed> negativeSpeed = Quantities.getQuantity(-0.5, Units.METRE_PER_SECOND);
        
        boolean result = validator.isValid(negativeSpeed, null);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true for positive decimal Quantity")
    void shouldReturnTrueForPositiveDecimalQuantity() {
        Quantity<Speed> speed = Quantities.getQuantity(2.5, Units.METRE_PER_SECOND);
        
        boolean result = validator.isValid(speed, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for Quantity with value exactly 0.0001")
    void shouldReturnTrueForQuantityWithValueExactlyPointZeroZeroZeroOne() {
        Quantity<Mass> mass = Quantities.getQuantity(0.0001, Units.GRAM);
        
        boolean result = validator.isValid(mass, null);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for Quantity with value exactly -0.0001")
    void shouldReturnFalseForQuantityWithValueExactlyNegativePointZeroZeroZeroOne() {
        Quantity<Mass> mass = Quantities.getQuantity(-0.0001, Units.GRAM);
        
        boolean result = validator.isValid(mass, null);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle initialize with Positive annotation")
    void shouldHandleInitializeWithPositiveAnnotation() {
        // Create a mock Positive annotation (we pass null since initialize doesn't use it)
        validator.initialize(null);
        
        Quantity<Mass> mass = Quantities.getQuantity(100, Units.GRAM);
        boolean result = validator.isValid(mass, null);
        
        assertTrue(result);
    }
}

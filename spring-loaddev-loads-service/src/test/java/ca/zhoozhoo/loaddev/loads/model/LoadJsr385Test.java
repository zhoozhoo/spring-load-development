package ca.zhoozhoo.loaddev.loads.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static tech.units.indriya.unit.Units.METRE;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link LoadJsr385} model class.
 * <p>
 * Tests validation logic, equals/hashCode contracts, and JSR-363 Quantity handling.
 * Note: Uses SI units (grams, meters) as imperial units require additional dependencies.
 * </p>
 *
 * @author Zhubin Salehi
 */
@DisplayName("LoadJsr385 Model Tests")
class LoadJsr385Test {

    // Test data constants
    private static final String OWNER_ID = "user123";
    private static final String NAME = "6.5 Creedmoor Match Load";
    private static final String DESCRIPTION = "Long range precision load";
    private static final String POWDER_MFG = "Hodgdon";
    private static final String POWDER_TYPE = "H4350";
    private static final String BULLET_MFG = "Hornady";
    private static final String BULLET_TYPE = "ELD Match";
    private static final String PRIMER_MFG = "CCI";
    private static final String PRIMER_TYPE = "BR-2";

    /**
     * Creates a valid LoadJsr385 instance for testing with metric units (grams and millimeters).
     */
    private LoadJsr385 createValidLoadMetric() {
        return new LoadJsr385(
            1L,
            OWNER_ID,
            NAME,
            DESCRIPTION,
            POWDER_MFG,
            POWDER_TYPE,
            BULLET_MFG,
            BULLET_TYPE,
            getQuantity(9.07, GRAM), // ~140 grains
            PRIMER_MFG,
            PRIMER_TYPE,
            getQuantity(0.508, METRE.divide(1000)), // 0.020 inches in mm
            getQuantity(71.12, METRE.divide(1000)), // 2.800 inches in mm
            getQuantity(0.051, METRE.divide(1000)), // 0.002 inches in mm
            1L
        );
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should create valid load with all fields")
        void shouldCreateValidLoadWithAllFields() {
            assertDoesNotThrow(() -> createValidLoadMetric());
        }

        @Test
        @DisplayName("Should create valid load with only distance from lands")
        void shouldCreateValidLoadWithOnlyDistanceFromLands() {
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                null, // no case overall length
                getQuantity(0.051, METRE.divide(1000)),
                null
            ));
        }

        @Test
        @DisplayName("Should create valid load with only case overall length")
        void shouldCreateValidLoadWithOnlyCaseOverallLength() {
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                null, // no distance from lands
                getQuantity(71.12, METRE.divide(1000)),
                getQuantity(0.051, METRE.divide(1000)),
                null
            ));
        }

        @Test
        @DisplayName("Should throw exception when both distance from lands and case overall length are null")
        void shouldThrowExceptionWhenBothMeasurementsAreNull() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new LoadJsr385(
                    null, OWNER_ID, NAME, DESCRIPTION,
                    POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                    getQuantity(9.07, GRAM),
                    PRIMER_MFG, PRIMER_TYPE,
                    null, // no distance from lands
                    null, // no case overall length
                    getQuantity(0.051, METRE.divide(1000)),
                    null
                )
            );
            
            assertEquals("Either distance from lands or case overall length must be specified",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when neck tension is negative")
        void shouldThrowExceptionWhenNeckTensionIsNegative() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new LoadJsr385(
                    null, OWNER_ID, NAME, DESCRIPTION,
                    POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                    getQuantity(9.07, GRAM),
                    PRIMER_MFG, PRIMER_TYPE,
                    getQuantity(0.508, METRE.divide(1000)),
                    getQuantity(71.12, METRE.divide(1000)),
                    getQuantity(-0.051, METRE.divide(1000)), // negative!
                    null
                )
            );
            
            assertTrue(exception.getMessage().contains("Neck tension must be positive"));
        }

        @Test
        @DisplayName("Should throw exception when neck tension is zero")
        void shouldThrowExceptionWhenNeckTensionIsZero() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new LoadJsr385(
                    null, OWNER_ID, NAME, DESCRIPTION,
                    POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                    getQuantity(9.07, GRAM),
                    PRIMER_MFG, PRIMER_TYPE,
                    getQuantity(0.508, METRE.divide(1000)),
                    getQuantity(71.12, METRE.divide(1000)),
                    getQuantity(0.0, METRE.divide(1000)), // zero!
                    null
                )
            );
            
            assertTrue(exception.getMessage().contains("Neck tension must be positive"));
        }

        @Test
        @DisplayName("Should accept null neck tension")
        void shouldAcceptNullNeckTension() {
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                getQuantity(71.12, METRE.divide(1000)),
                null, // null neck tension is allowed
                null
            ));
        }
    }

    @Nested
    @DisplayName("Quantity Handling Tests")
    class QuantityHandlingTests {

        @Test
        @DisplayName("Should handle metric units correctly")
        void shouldHandleMetricUnitsCorrectly() {
            LoadJsr385 load = createValidLoadMetric();
            
            assertEquals(9.07, load.bulletWeight().getValue().doubleValue(), 0.01);
            assertEquals(GRAM, load.bulletWeight().getUnit());
            
            assertEquals(0.508, load.distanceFromLands().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle different metric length measurements")
        void shouldHandleDifferentMetricLengthMeasurements() {
            LoadJsr385 load = createValidLoadMetric();
            
            assertEquals(0.508, load.distanceFromLands().getValue().doubleValue(), 0.001);
            assertEquals(71.12, load.caseOverallLength().getValue().doubleValue(), 0.01);
            assertEquals(0.051, load.neckTension().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should support unit conversion for bullet weight")
        void shouldSupportUnitConversionForBulletWeight() {
            LoadJsr385 load = createValidLoadMetric();
            
            Quantity<Mass> weightInGrams = load.bulletWeight();
            Quantity<Mass> weightInKg = weightInGrams.to(KILOGRAM);
            
            assertEquals(0.00907, weightInKg.getValue().doubleValue(), 0.00001);
        }

        @Test
        @DisplayName("Should support unit conversion for length measurements")
        void shouldSupportUnitConversionForLengthMeasurements() {
            LoadJsr385 load = createValidLoadMetric();
            
            Quantity<Length> lengthInMm = load.caseOverallLength();
            Quantity<Length> lengthInMeters = lengthInMm.to(METRE);
            
            assertEquals(0.07112, lengthInMeters.getValue().doubleValue(), 0.00001);
        }

        @Test
        @DisplayName("Should allow mixed units in same load")
        void shouldAllowMixedUnitsInSameLoad() {
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)), // mm
                getQuantity(0.07112, METRE), // meters
                getQuantity(51, METRE.divide(1000000)), // micrometers
                null
            ));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            LoadJsr385 load = createValidLoadMetric();
            assertEquals(load, load);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            LoadJsr385 load = createValidLoadMetric();
            assertNotEquals(null, load);
        }

        @Test
        @DisplayName("Should not equal different class")
        void shouldNotEqualDifferentClass() {
            LoadJsr385 load = createValidLoadMetric();
            assertNotEquals(load, "Not a LoadJsr385");
        }

        @Test
        @DisplayName("Should be equal when all fields match except ownerId")
        void shouldBeEqualWhenAllFieldsMatchExceptOwnerId() {
            LoadJsr385 load1 = new LoadJsr385(
                1L, "user1", NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                getQuantity(71.12, METRE.divide(1000)),
                getQuantity(0.051, METRE.divide(1000)),
                1L
            );
            
            LoadJsr385 load2 = new LoadJsr385(
                1L, "user2", NAME, DESCRIPTION, // different ownerId
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                getQuantity(71.12, METRE.divide(1000)),
                getQuantity(0.051, METRE.divide(1000)),
                1L
            );
            
            assertEquals(load1, load2);
        }

        @Test
        @DisplayName("Should not be equal when Quantity values differ")
        void shouldNotBeEqualWhenQuantityValuesDiffer() {
            LoadJsr385 load1 = createValidLoadMetric();
            
            LoadJsr385 load2 = new LoadJsr385(
                1L, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(10.0, GRAM), // different bullet weight
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                getQuantity(71.12, METRE.divide(1000)),
                getQuantity(0.051, METRE.divide(1000)),
                1L
            );
            
            assertNotEquals(load1, load2);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            LoadJsr385 load = createValidLoadMetric();
            int hashCode1 = load.hashCode();
            int hashCode2 = load.hashCode();
            assertEquals(hashCode1, hashCode2);
        }

        @Test
        @DisplayName("Equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            LoadJsr385 load1 = createValidLoadMetric();
            LoadJsr385 load2 = createValidLoadMetric();
            
            assertEquals(load1, load2);
            assertEquals(load1.hashCode(), load2.hashCode());
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("Should return correct values from accessors")
        void shouldReturnCorrectValuesFromAccessors() {
            LoadJsr385 load = createValidLoadMetric();
            
            assertEquals(1L, load.id());
            assertEquals(OWNER_ID, load.ownerId());
            assertEquals(NAME, load.name());
            assertEquals(DESCRIPTION, load.description());
            assertEquals(POWDER_MFG, load.powderManufacturer());
            assertEquals(POWDER_TYPE, load.powderType());
            assertEquals(BULLET_MFG, load.bulletManufacturer());
            assertEquals(BULLET_TYPE, load.bulletType());
            assertNotNull(load.bulletWeight());
            assertEquals(PRIMER_MFG, load.primerManufacturer());
            assertEquals(PRIMER_TYPE, load.primerType());
            assertNotNull(load.distanceFromLands());
            assertNotNull(load.caseOverallLength());
            assertNotNull(load.neckTension());
            assertEquals(1L, load.rifleId());
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            LoadJsr385 load = new LoadJsr385(
                null, OWNER_ID, NAME, null, // null description
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                null, // null case overall length
                null, // null neck tension
                null  // null rifleId
            );
            
            assertEquals(null, load.description());
            assertEquals(null, load.caseOverallLength());
            assertEquals(null, load.neckTension());
            assertEquals(null, load.rifleId());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very small Quantity values")
        void shouldHandleVerySmallQuantityValues() {
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(0.001, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.001, METRE.divide(1000)),
                getQuantity(0.001, METRE.divide(1000)),
                getQuantity(0.001, METRE.divide(1000)),
                null
            ));
        }

        @Test
        @DisplayName("Should handle very large Quantity values")
        void shouldHandleVeryLargeQuantityValues() {
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, NAME, DESCRIPTION,
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(1000.0, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(250.0, METRE.divide(1000)),
                getQuantity(250.0, METRE.divide(1000)),
                getQuantity(25.0, METRE.divide(1000)),
                null
            ));
        }

        @Test
        @DisplayName("Should handle empty strings for required fields")
        void shouldHandleEmptyStringsForRequiredFields() {
            // Note: Bean validation would catch this in a real scenario
            assertDoesNotThrow(() -> new LoadJsr385(
                null, OWNER_ID, "", "", // empty name and description
                POWDER_MFG, POWDER_TYPE, BULLET_MFG, BULLET_TYPE,
                getQuantity(9.07, GRAM),
                PRIMER_MFG, PRIMER_TYPE,
                getQuantity(0.508, METRE.divide(1000)),
                getQuantity(71.12, METRE.divide(1000)),
                null,
                null
            ));
        }
    }
}

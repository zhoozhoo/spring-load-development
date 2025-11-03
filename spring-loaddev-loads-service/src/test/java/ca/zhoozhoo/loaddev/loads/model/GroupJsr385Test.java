package ca.zhoozhoo.loaddev.loads.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.METRE;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link GroupJsr385} model class.
 * <p>
 * Tests validation logic, equals/hashCode contracts, and JSR-385 Quantity handling
 * for shooting group records with powder charge, target range, and group size measurements.
 * </p>
 *
 * @author Zhubin Salehi
 */
@DisplayName("GroupJsr385 Model Tests")
class GroupJsr385Test {

    // Test data constants
    private static final String OWNER_ID = "user123";
    private static final Long LOAD_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 11, 1);

    /**
     * Creates a valid GroupJsr385 instance for testing using imperial units.
     */
    private GroupJsr385 createValidGroup() {
        return new GroupJsr385(
            1L,
            OWNER_ID,
            LOAD_ID,
            TEST_DATE,
            getQuantity(42.5, GRAIN),         // powder charge
            getQuantity(100, YARD_INTERNATIONAL), // target range
            getQuantity(0.75, INCH_INTERNATIONAL) // group size
        );
    }

    /**
     * Creates a valid GroupJsr385 instance using metric units.
     */
    private GroupJsr385 createValidGroupMetric() {
        return new GroupJsr385(
            2L,
            OWNER_ID,
            LOAD_ID,
            TEST_DATE,
            getQuantity(2.75, GRAM),           // ~42.5 grains
            getQuantity(91.44, METRE),         // 100 yards in meters
            getQuantity(19.05, METRE.divide(1000)) // 0.75 inches in mm
        );
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should create valid group with all fields")
        void shouldCreateValidGroupWithAllFields() {
            assertDoesNotThrow(() -> createValidGroup());
        }

        @Test
        @DisplayName("Should create valid group with metric units")
        void shouldCreateValidGroupWithMetricUnits() {
            assertDoesNotThrow(() -> createValidGroupMetric());
        }

        @Test
        @DisplayName("Should throw exception when date is in the future")
        void shouldThrowExceptionWhenDateIsInFuture() {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    futureDate,
                    getQuantity(42.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.75, INCH_INTERNATIONAL)
                )
            );
            
            assertEquals("Group date cannot be in the future", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept today's date")
        void shouldAcceptTodaysDate() {
            assertDoesNotThrow(() -> new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                LocalDate.now(),
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            ));
        }

        @Test
        @DisplayName("Should throw exception when powder charge is below minimum (0.1 grains)")
        void shouldThrowExceptionWhenPowderChargeTooLow() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(0.05, GRAIN), // below minimum
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.75, INCH_INTERNATIONAL)
                )
            );
            
            assertTrue(exception.getMessage().contains("Powder charge must be between 0.1 and 150.0 grains"));
        }

        @Test
        @DisplayName("Should throw exception when powder charge is above maximum (150 grains)")
        void shouldThrowExceptionWhenPowderChargeTooHigh() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(151.0, GRAIN), // above maximum
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.75, INCH_INTERNATIONAL)
                )
            );
            
            assertTrue(exception.getMessage().contains("Powder charge must be between 0.1 and 150.0 grains"));
        }

        @Test
        @DisplayName("Should accept minimum powder charge (0.1 grains)")
        void shouldAcceptMinimumPowderCharge() {
            assertDoesNotThrow(() -> new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(0.1, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            ));
        }

        @Test
        @DisplayName("Should accept maximum powder charge (150 grains)")
        void shouldAcceptMaximumPowderCharge() {
            assertDoesNotThrow(() -> new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(150.0, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            ));
        }

        @Test
        @DisplayName("Should throw exception when target range is below minimum (10 yards)")
        void shouldThrowExceptionWhenTargetRangeTooShort() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(42.5, GRAIN),
                    getQuantity(9, YARD_INTERNATIONAL), // below minimum
                    getQuantity(0.75, INCH_INTERNATIONAL)
                )
            );
            
            assertTrue(exception.getMessage().contains("Target range must be between 10 and 2000 yards"));
        }

        @Test
        @DisplayName("Should throw exception when target range is above maximum (2000 yards)")
        void shouldThrowExceptionWhenTargetRangeTooLong() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(42.5, GRAIN),
                    getQuantity(2001, YARD_INTERNATIONAL), // above maximum
                    getQuantity(0.75, INCH_INTERNATIONAL)
                )
            );
            
            assertTrue(exception.getMessage().contains("Target range must be between 10 and 2000 yards"));
        }

        @Test
        @DisplayName("Should throw exception when group size is below minimum (0.01 inches)")
        void shouldThrowExceptionWhenGroupSizeTooSmall() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(42.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.005, INCH_INTERNATIONAL) // below minimum
                )
            );
            
            assertTrue(exception.getMessage().contains("Group size must be between 0.01 and 50.0 inches"));
        }

        @Test
        @DisplayName("Should throw exception when group size is above maximum (50 inches)")
        void shouldThrowExceptionWhenGroupSizeTooLarge() {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new GroupJsr385(
                    null,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(42.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(51.0, INCH_INTERNATIONAL) // above maximum
                )
            );
            
            assertTrue(exception.getMessage().contains("Group size must be between 0.01 and 50.0 inches"));
        }

        @Test
        @DisplayName("Should accept null group size")
        void shouldAcceptNullGroupSize() {
            assertDoesNotThrow(() -> new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                null // null group size is allowed
            ));
        }
    }

    @Nested
    @DisplayName("Quantity Handling Tests")
    class QuantityHandlingTests {

        @Test
        @DisplayName("Should handle imperial units correctly")
        void shouldHandleImperialUnitsCorrectly() {
            var group = createValidGroup();
            
            assertEquals(42.5, group.powderCharge().getValue().doubleValue(), 0.01);
            assertEquals(GRAIN, group.powderCharge().getUnit());
            
            assertEquals(100, group.targetRange().getValue().doubleValue(), 0.01);
            assertEquals(YARD_INTERNATIONAL, group.targetRange().getUnit());
            
            assertEquals(0.75, group.groupSize().getValue().doubleValue(), 0.01);
            assertEquals(INCH_INTERNATIONAL, group.groupSize().getUnit());
        }

        @Test
        @DisplayName("Should handle metric units correctly")
        void shouldHandleMetricUnitsCorrectly() {
            var group = createValidGroupMetric();
            
            assertEquals(2.75, group.powderCharge().getValue().doubleValue(), 0.01);
            assertEquals(GRAM, group.powderCharge().getUnit());
            
            assertEquals(91.44, group.targetRange().getValue().doubleValue(), 0.01);
            assertEquals(METRE, group.targetRange().getUnit());
        }

        @Test
        @DisplayName("Should support unit conversion for powder charge")
        void shouldSupportUnitConversionForPowderCharge() {
            var group = createValidGroup();
            
            assertEquals(2.754, group.powderCharge().to(GRAM).getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should support unit conversion for target range")
        void shouldSupportUnitConversionForTargetRange() {
            var group = createValidGroup();
            
            assertEquals(91.44, group.targetRange().to(METRE).getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should support unit conversion for group size")
        void shouldSupportUnitConversionForGroupSize() {
            var group = createValidGroup();
            
            assertEquals(19.05, group.groupSize().to(METRE.divide(1000)).getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should allow mixed units in same group")
        void shouldAllowMixedUnitsInSameGroup() {
            assertDoesNotThrow(() -> new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),                    // imperial
                getQuantity(91.44, METRE),                   // metric
                getQuantity(19.05, METRE.divide(1000))       // metric mm
            ));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            var group = createValidGroup();
            assertEquals(group, group);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            var group = createValidGroup();
            assertNotEquals(null, group);
        }

        @Test
        @DisplayName("Should not equal different class")
        void shouldNotEqualDifferentClass() {
            var group = createValidGroup();
            assertNotEquals(group, "Not a GroupJsr385");
        }

        @Test
        @DisplayName("Should be equal when all fields match except ownerId")
        void shouldBeEqualWhenAllFieldsMatchExceptOwnerId() {
            var group1 = new GroupJsr385(
                1L,
                "user1",
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            var group2 = new GroupJsr385(
                1L,
                "user2", // different ownerId
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertEquals(group1, group2);
        }

        @Test
        @DisplayName("Should not be equal when powder charge differs")
        void shouldNotBeEqualWhenPowderChargeDiffers() {
            var group1 = createValidGroup();
            
            var group2 = new GroupJsr385(
                1L,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(43.0, GRAIN), // different powder charge
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertNotEquals(group1, group2);
        }

        @Test
        @DisplayName("Should not be equal when target range differs")
        void shouldNotBeEqualWhenTargetRangeDiffers() {
            var group1 = createValidGroup();
            
            var group2 = new GroupJsr385(
                1L,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(200, YARD_INTERNATIONAL), // different target range
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertNotEquals(group1, group2);
        }

        @Test
        @DisplayName("Should not be equal when date differs")
        void shouldNotBeEqualWhenDateDiffers() {
            var group1 = createValidGroup();
            
            var group2 = new GroupJsr385(
                1L,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE.minusDays(1), // different date
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertNotEquals(group1, group2);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            var group = createValidGroup();
            assertEquals(group.hashCode(), group.hashCode());
        }

        @Test
        @DisplayName("Equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            var group1 = createValidGroup();
            var group2 = createValidGroup();
            
            assertEquals(group1, group2);
            assertEquals(group1.hashCode(), group2.hashCode());
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("Should return correct values from accessors")
        void shouldReturnCorrectValuesFromAccessors() {
            var group = createValidGroup();
            
            assertEquals(1L, group.id());
            assertEquals(OWNER_ID, group.ownerId());
            assertEquals(LOAD_ID, group.loadId());
            assertEquals(TEST_DATE, group.date());
            assertNotNull(group.powderCharge());
            assertNotNull(group.targetRange());
            assertNotNull(group.groupSize());
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            var group = new GroupJsr385(
                null, // null id
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                null  // null group size
            );
            
            assertEquals(null, group.id());
            assertEquals(null, group.groupSize());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle minimum valid powder charge")
        void shouldHandleMinimumValidPowderCharge() {
            var group = new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(0.1, GRAIN), // minimum
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertEquals(0.1, group.powderCharge().to(GRAIN).getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle maximum valid powder charge")
        void shouldHandleMaximumValidPowderCharge() {
            var group = new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(150.0, GRAIN), // maximum
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertEquals(150.0, group.powderCharge().to(GRAIN).getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle minimum valid target range")
        void shouldHandleMinimumValidTargetRange() {
            var group = new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(10, YARD_INTERNATIONAL), // minimum
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertEquals(10, group.targetRange().to(YARD_INTERNATIONAL).getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle maximum valid target range")
        void shouldHandleMaximumValidTargetRange() {
            var group = new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(2000, YARD_INTERNATIONAL), // maximum
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertEquals(2000, group.targetRange().to(YARD_INTERNATIONAL).getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle very precise group size measurements")
        void shouldHandleVeryPreciseGroupSizeMeasurements() {
            var group = new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                TEST_DATE,
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.01, INCH_INTERNATIONAL) // minimum precision
            );
            
            assertEquals(0.01, group.groupSize().to(INCH_INTERNATIONAL).getValue().doubleValue(), 0.0001);
        }

        @Test
        @DisplayName("Should handle past dates correctly")
        void shouldHandlePastDatesCorrectly() {
            var pastDate = LocalDate.of(2020, 1, 1);
            
            var group = new GroupJsr385(
                null,
                OWNER_ID,
                LOAD_ID,
                pastDate,
                getQuantity(42.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)
            );
            
            assertEquals(pastDate, group.date());
        }
    }
}

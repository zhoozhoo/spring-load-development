package ca.zhoozhoo.loaddev.loads.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Shot} model class.
 * <p>
 * Tests validation logic, equals/hashCode contracts,
 * for individual shot velocity measurements.
 * </p>
 *
 * @author Zhubin Salehi
 */
@DisplayName("Shot Model Tests")
class ShotTest {

    // Test data constants
    private static final String OWNER_ID = "user123";
    private static final Long GROUP_ID = 1L;

    /**
     * Creates a valid Shot instance for testing with feet per second.
     */
    @SuppressWarnings("unchecked")
    private Shot createValidShot() {
        return new Shot(
            1L,
            OWNER_ID,
            GROUP_ID,
            getQuantity(2800, (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND))
        );
    }

    /**
     * Creates a valid Shot instance using metric units (meters per second).
     */
    @SuppressWarnings("unchecked")
    private Shot createValidShotMetric() {
        return new Shot(
            2L,
            OWNER_ID,
            GROUP_ID,
            getQuantity(853.44, (Unit<Speed>) METRE.divide(SECOND))
        );
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should create valid shot with all fields")
        void shouldCreateValidShotWithAllFields() {
            assertDoesNotThrow(() -> createValidShot());
        }

        @Test
        @DisplayName("Should create valid shot with metric units")
        void shouldCreateValidShotWithMetricUnits() {
            assertDoesNotThrow(() -> createValidShotMetric());
        }

        @Test
        @DisplayName("Should throw exception when velocity is below minimum (500 fps)")
        void shouldThrowExceptionWhenVelocityTooLow() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new Shot(null, OWNER_ID, GROUP_ID, getQuantity(499, feetPerSecond))
            );
            
            assertTrue(exception.getMessage().contains("Velocity must be between 500 and 5000 fps"));
        }

        @Test
        @DisplayName("Should throw exception when velocity is above maximum (5000 fps)")
        void shouldThrowExceptionWhenVelocityTooHigh() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var exception = assertThrows(IllegalArgumentException.class, () ->
                new Shot(null, OWNER_ID, GROUP_ID, getQuantity(5001, feetPerSecond))
            );
            
            assertTrue(exception.getMessage().contains("Velocity must be between 500 and 5000 fps"));
        }

        @Test
        @DisplayName("Should accept minimum velocity (500 fps)")
        void shouldAcceptMinimumVelocity() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(500, feetPerSecond)));
        }

        @Test
        @DisplayName("Should accept maximum velocity (5000 fps)")
        void shouldAcceptMaximumVelocity() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(5000, feetPerSecond)));
        }

        @Test
        @DisplayName("Should accept typical rifle velocities")
        void shouldAcceptTypicalRifleVelocities() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            // Test various common rifle velocities
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(800, feetPerSecond)));   // .45 ACP
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(1200, feetPerSecond)));  // 9mm
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(2700, feetPerSecond)));  // .308 Win
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(3200, feetPerSecond)));  // .223 Rem
        }

        @Test
        @DisplayName("Should validate velocity in metric units correctly")
        void shouldValidateVelocityInMetricUnitsCorrectly() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            // 152.4 m/s = 500 fps (minimum)
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(152.4, metersPerSecond)));
            
            // 1524 m/s = 5000 fps (maximum)
            assertDoesNotThrow(() -> new Shot(null, OWNER_ID, GROUP_ID, getQuantity(1524, metersPerSecond)));
            
            // Below minimum
            assertThrows(IllegalArgumentException.class, () -> 
                new Shot(null, OWNER_ID, GROUP_ID, getQuantity(150, metersPerSecond)));
            
            // Above maximum
            assertThrows(IllegalArgumentException.class, () -> 
                new Shot(null, OWNER_ID, GROUP_ID, getQuantity(1530, metersPerSecond)));
        }
    }

    @Nested
    @DisplayName("Quantity Handling Tests")
    class QuantityHandlingTests {

        @Test
        @DisplayName("Should handle imperial units correctly")
        void shouldHandleImperialUnitsCorrectly() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = createValidShot();
            
            assertEquals(2800, shot.velocity().getValue().doubleValue(), 0.01);
            assertEquals(feetPerSecond, shot.velocity().getUnit());
        }

        @Test
        @DisplayName("Should handle metric units correctly")
        void shouldHandleMetricUnitsCorrectly() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            var shot = createValidShotMetric();
            
            assertEquals(853.44, shot.velocity().getValue().doubleValue(), 0.01);
            assertEquals(metersPerSecond, shot.velocity().getUnit());
        }

        @Test
        @DisplayName("Should support unit conversion from fps to m/s")
        void shouldSupportUnitConversionFromFpsToMps() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            var shot = createValidShot();
            
            assertEquals(853.44, shot.velocity().to(metersPerSecond).getValue().doubleValue(), 0.1);
        }

        @Test
        @DisplayName("Should support unit conversion from m/s to fps")
        void shouldSupportUnitConversionFromMpsToFps() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = createValidShotMetric();
            
            assertEquals(2800, shot.velocity().to(feetPerSecond).getValue().doubleValue(), 1.0);
        }

        @Test
        @DisplayName("Should preserve velocity precision")
        void shouldPreserveVelocityPrecision() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(2845.67, feetPerSecond));
            
            assertEquals(2845.67, shot.velocity().getValue().doubleValue(), 0.01);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            var shot = createValidShot();
            assertEquals(shot, shot);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            var shot = createValidShot();
            assertNotEquals(null, shot);
        }

        @Test
        @DisplayName("Should not equal different class")
        void shouldNotEqualDifferentClass() {
            var shot = createValidShot();
            assertNotEquals(shot, "Not a Shot");
        }

        @Test
        @DisplayName("Should be equal when all fields match except ownerId")
        void shouldBeEqualWhenAllFieldsMatchExceptOwnerId() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot1 = new Shot(
                1L,
                "user1",
                GROUP_ID,
                getQuantity(2800, feetPerSecond)
            );
            
            var shot2 = new Shot(
                1L,
                "user2", // different ownerId
                GROUP_ID,
                getQuantity(2800, feetPerSecond)
            );
            
            assertEquals(shot1, shot2);
        }

        @Test
        @DisplayName("Should not be equal when velocity differs")
        void shouldNotBeEqualWhenVelocityDiffers() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot1 = createValidShot();
            
            var shot2 = new Shot(
                1L,
                OWNER_ID,
                GROUP_ID,
                getQuantity(2850, feetPerSecond) // different velocity
            );
            
            assertNotEquals(shot1, shot2);
        }

        @Test
        @DisplayName("Should not be equal when group ID differs")
        void shouldNotBeEqualWhenGroupIdDiffers() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot1 = createValidShot();
            
            var shot2 = new Shot(
                1L,
                OWNER_ID,
                2L, // different group ID
                getQuantity(2800, feetPerSecond)
            );
            
            assertNotEquals(shot1, shot2);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            var shot = createValidShot();
            assertEquals(shot.hashCode(), shot.hashCode());
        }

        @Test
        @DisplayName("Equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            var shot1 = createValidShot();
            var shot2 = createValidShot();
            
            assertEquals(shot1, shot2);
            assertEquals(shot1.hashCode(), shot2.hashCode());
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("Should return correct values from accessors")
        void shouldReturnCorrectValuesFromAccessors() {
            var shot = createValidShot();
            
            assertEquals(1L, shot.id());
            assertEquals(OWNER_ID, shot.ownerId());
            assertEquals(GROUP_ID, shot.groupId());
            assertNotNull(shot.velocity());
        }

        @Test
        @DisplayName("Should handle null id")
        void shouldHandleNullId() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(2800, feetPerSecond));
            
            assertEquals(null, shot.id());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle minimum valid velocity")
        void shouldHandleMinimumValidVelocity() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(500, feetPerSecond));
            
            assertEquals(500, shot.velocity().to(feetPerSecond).getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle maximum valid velocity")
        void shouldHandleMaximumValidVelocity() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(5000, feetPerSecond));
            
            assertEquals(5000, shot.velocity().to(feetPerSecond).getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle high precision velocity values")
        void shouldHandleHighPrecisionVelocityValues() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var shot = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(2845.6789, feetPerSecond));
            
            assertEquals(2845.6789, shot.velocity().getValue().doubleValue(), 0.0001);
        }

        @Test
        @DisplayName("Should handle common handgun velocities")
        void shouldHandleCommonHandgunVelocities() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            // .22 LR
            var shot22lr = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(1070, feetPerSecond));
            assertEquals(1070, shot22lr.velocity().to(feetPerSecond).getValue().doubleValue(), 0.1);
            
            // 9mm
            var shot9mm = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(1150, feetPerSecond));
            assertEquals(1150, shot9mm.velocity().to(feetPerSecond).getValue().doubleValue(), 0.1);
            
            // .45 ACP
            var shot45acp = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(850, feetPerSecond));
            assertEquals(850, shot45acp.velocity().to(feetPerSecond).getValue().doubleValue(), 0.1);
        }

        @Test
        @DisplayName("Should handle common rifle velocities")
        void shouldHandleCommonRifleVelocities() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            // .308 Winchester
            var shot308 = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(2700, feetPerSecond));
            assertEquals(2700, shot308.velocity().to(feetPerSecond).getValue().doubleValue(), 0.1);
            
            // .223 Remington
            var shot223 = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(3200, feetPerSecond));
            assertEquals(3200, shot223.velocity().to(feetPerSecond).getValue().doubleValue(), 0.1);
            
            // 6.5 Creedmoor
            var shot65cm = new Shot(null, OWNER_ID, GROUP_ID, getQuantity(2800, feetPerSecond));
            assertEquals(2800, shot65cm.velocity().to(feetPerSecond).getValue().doubleValue(), 0.1);
        }

        @Test
        @DisplayName("Should reject zero velocity")
        void shouldRejectZeroVelocity() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            assertThrows(IllegalArgumentException.class, () ->
                new Shot(null, OWNER_ID, GROUP_ID, getQuantity(0, feetPerSecond))
            );
        }

        @Test
        @DisplayName("Should reject negative velocity")
        void shouldRejectNegativeVelocity() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            assertThrows(IllegalArgumentException.class, () ->
                new Shot(null, OWNER_ID, GROUP_ID, getQuantity(-100, feetPerSecond))
            );
        }
    }
}

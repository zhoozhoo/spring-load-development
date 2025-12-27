package ca.zhoozhoo.loaddev.loads.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link GroupStatistics} model class.
 * <p>
 * Tests defensive copying, immutability, equals/hashCode contracts
 * for ballistic statistics including average velocity, standard deviation, and extreme spread.
 * </p>
 *
 * @author Zhubin Salehi
 */
@DisplayName("GroupStatistics Model Tests")
class GroupStatisticsTest {

    // Test data constants
    private static final String OWNER_ID = "user123";
    private static final Long GROUP_ID = 1L;
    private static final Long LOAD_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 11, 1);

    /**
     * Creates a valid Group instance for testing.
     */
    private Group createTestGroup() {
        return new Group(
            GROUP_ID,
            OWNER_ID,
            LOAD_ID,
            TEST_DATE,
            getQuantity(42.5, GRAIN),
            getQuantity(100, YARD_INTERNATIONAL),
            getQuantity(0.75, INCH_INTERNATIONAL)
        );
    }

    /**
     * Creates a list of valid Shot instances for testing.
     */
    private List<Shot> createTestShots() {
        @SuppressWarnings("unchecked")
        Unit<Speed> feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
        
        return List.of(
            new Shot(1L, OWNER_ID, GROUP_ID, getQuantity(2800, feetPerSecond)),
            new Shot(2L, OWNER_ID, GROUP_ID, getQuantity(2805, feetPerSecond)),
            new Shot(3L, OWNER_ID, GROUP_ID, getQuantity(2795, feetPerSecond)),
            new Shot(4L, OWNER_ID, GROUP_ID, getQuantity(2810, feetPerSecond)),
            new Shot(5L, OWNER_ID, GROUP_ID, getQuantity(2790, feetPerSecond))
        );
    }

    /**
     * Creates a valid GroupStatistics instance for testing.
     */
    private GroupStatistics createValidStatistics() {
        @SuppressWarnings("unchecked")
        Unit<Speed> feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
        
        return new GroupStatistics(
            createTestGroup(),
            getQuantity(2800, feetPerSecond),  // average velocity
            getQuantity(7.91, feetPerSecond),  // standard deviation
            getQuantity(20, feetPerSecond),    // extreme spread
            createTestShots()
        );
    }

    @Nested
    @DisplayName("Constructor and Defensive Copy Tests")
    class ConstructorAndDefensiveCopyTests {

        @Test
        @DisplayName("Should create valid statistics with all fields")
        void shouldCreateValidStatisticsWithAllFields() {
            assertDoesNotThrow(() -> createValidStatistics());
        }

        @Test
        @DisplayName("Should create defensive copy of shots list")
        void shouldCreateDefensiveCopyOfShotsList() {
            var originalShots = new ArrayList<>(createTestShots());
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, getFeetPerSecond()),
                getQuantity(7.91, getFeetPerSecond()),
                getQuantity(20, getFeetPerSecond()),
                originalShots
            );
            
            // Modify the original list
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            originalShots.add(new Shot(6L, OWNER_ID, GROUP_ID, getQuantity(2815, feetPerSecond)));
            
            // Verify the stats object's list was not affected
            assertEquals(5, stats.shots().size());
            assertNotEquals(originalShots.size(), stats.shots().size());
        }

        @Test
        @DisplayName("Should return immutable shots list")
        void shouldReturnImmutableShotsList() {
            var stats = createValidStatistics();
            
            // Attempting to modify the returned list should throw
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            assertThrows(
                UnsupportedOperationException.class,
                () -> stats.shots().add(new Shot(6L, OWNER_ID, GROUP_ID, getQuantity(2815, feetPerSecond)))
            );
        }

        @Test
        @DisplayName("Should handle null shots list")
        void shouldHandleNullShotsList() {
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, getFeetPerSecond()),
                getQuantity(7.91, getFeetPerSecond()),
                getQuantity(20, getFeetPerSecond()),
                null
            );
            
            assertNotNull(stats.shots());
            assertEquals(0, stats.shots().size());
        }

        @Test
        @DisplayName("Should handle empty shots list")
        void shouldHandleEmptyShotsList() {
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, getFeetPerSecond()),
                getQuantity(7.91, getFeetPerSecond()),
                getQuantity(20, getFeetPerSecond()),
                List.of()
            );
            
            assertNotNull(stats.shots());
            assertEquals(0, stats.shots().size());
        }

        @SuppressWarnings("unchecked")
        private Unit<Speed> getFeetPerSecond() {
            return (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
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
            
            var stats = createValidStatistics();
            
            assertEquals(2800, stats.averageVelocity().getValue().doubleValue(), 0.01);
            assertEquals(feetPerSecond, stats.averageVelocity().getUnit());
            
            assertEquals(7.91, stats.standardDeviation().getValue().doubleValue(), 0.01);
            assertEquals(feetPerSecond, stats.standardDeviation().getUnit());
            
            assertEquals(20, stats.extremeSpread().getValue().doubleValue(), 0.01);
            assertEquals(feetPerSecond, stats.extremeSpread().getUnit());
        }

        @Test
        @DisplayName("Should handle metric units correctly")
        void shouldHandleMetricUnitsCorrectly() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(853.44, metersPerSecond),  // ~2800 fps
                getQuantity(2.41, metersPerSecond),    // ~7.91 fps
                getQuantity(6.10, metersPerSecond),    // ~20 fps
                createTestShots()
            );
            
            assertEquals(853.44, stats.averageVelocity().getValue().doubleValue(), 0.01);
            assertEquals(metersPerSecond, stats.averageVelocity().getUnit());
        }

        @Test
        @DisplayName("Should support unit conversion for average velocity")
        void shouldSupportUnitConversionForAverageVelocity() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            var stats = createValidStatistics();
            
            assertEquals(853.44, stats.averageVelocity().to(metersPerSecond).getValue().doubleValue(), 1.0);
        }

        @Test
        @DisplayName("Should support unit conversion for standard deviation")
        void shouldSupportUnitConversionForStandardDeviation() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            var stats = createValidStatistics();
            
            assertEquals(2.41, stats.standardDeviation().to(metersPerSecond).getValue().doubleValue(), 0.1);
        }

        @Test
        @DisplayName("Should support unit conversion for extreme spread")
        void shouldSupportUnitConversionForExtremeSpread() {
            @SuppressWarnings("unchecked")
            var metersPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            var stats = createValidStatistics();
            
            assertEquals(6.10, stats.extremeSpread().to(metersPerSecond).getValue().doubleValue(), 0.1);
        }

        @Test
        @DisplayName("Should handle very low standard deviation")
        void shouldHandleVeryLowStandardDeviation() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            // High-quality load with very consistent velocities
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, feetPerSecond),
                getQuantity(2.5, feetPerSecond),  // excellent SD
                getQuantity(5, feetPerSecond),
                createTestShots()
            );
            
            assertEquals(2.5, stats.standardDeviation().getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should handle zero standard deviation")
        void shouldHandleZeroStandardDeviation() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            // Theoretical perfect consistency (all shots same velocity)
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, feetPerSecond),
                getQuantity(0, feetPerSecond),
                getQuantity(0, feetPerSecond),
                createTestShots()
            );
            
            assertEquals(0, stats.standardDeviation().getValue().doubleValue(), 0.001);
            assertEquals(0, stats.extremeSpread().getValue().doubleValue(), 0.001);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            var stats = createValidStatistics();
            assertEquals(stats, stats);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            var stats = createValidStatistics();
            assertNotEquals(null, stats);
        }

        @Test
        @DisplayName("Should not equal different class")
        void shouldNotEqualDifferentClass() {
            var stats = createValidStatistics();
            assertNotEquals(stats, "Not a GroupStatistics");
        }

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            var stats1 = createValidStatistics();
            var stats2 = createValidStatistics();
            
            assertEquals(stats1, stats2);
        }

        @Test
        @DisplayName("Should not be equal when average velocity differs")
        void shouldNotBeEqualWhenAverageVelocityDiffers() {
            @SuppressWarnings("unchecked")
            var fps = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var stats1 = createValidStatistics();
            
            var stats2 = new GroupStatistics(
                createTestGroup(),
                getQuantity(2850, fps),  // different average
                getQuantity(7.91, fps),
                getQuantity(20, fps),
                createTestShots()
            );
            
            assertNotEquals(stats1, stats2);
        }

        @Test
        @DisplayName("Should not be equal when shots list differs")
        void shouldNotBeEqualWhenShotsListDiffers() {
            @SuppressWarnings("unchecked")
            var fps = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var stats1 = createValidStatistics();
            
            var differentShots = List.of(
                new Shot(1L, OWNER_ID, GROUP_ID, getQuantity(2850, fps)),
                new Shot(2L, OWNER_ID, GROUP_ID, getQuantity(2855, fps))
            );
            
            var stats2 = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, fps),
                getQuantity(7.91, fps),
                getQuantity(20, fps),
                differentShots
            );
            
            assertNotEquals(stats1, stats2);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            var stats = createValidStatistics();
            assertEquals(stats.hashCode(), stats.hashCode());
        }

        @Test
        @DisplayName("Equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            var stats1 = createValidStatistics();
            var stats2 = createValidStatistics();
            
            assertEquals(stats1, stats2);
            assertEquals(stats1.hashCode(), stats2.hashCode());
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("Should return correct values from accessors")
        void shouldReturnCorrectValuesFromAccessors() {
            var stats = createValidStatistics();
            
            assertNotNull(stats.group());
            assertNotNull(stats.averageVelocity());
            assertNotNull(stats.standardDeviation());
            assertNotNull(stats.extremeSpread());
            assertNotNull(stats.shots());
            assertEquals(5, stats.shots().size());
        }

        @Test
        @DisplayName("Should return immutable group reference")
        void shouldReturnImmutableGroupReference() {
            var stats = createValidStatistics();
            var group = stats.group();
            
            assertNotNull(group);
            assertEquals(GROUP_ID, group.id());
        }

        @Test
        @DisplayName("Should return all shots in correct order")
        void shouldReturnAllShotsInCorrectOrder() {
            var stats = createValidStatistics();
            
            assertEquals(5, stats.shots().size());
            assertEquals(1L, stats.shots().get(0).id());
            assertEquals(2L, stats.shots().get(1).id());
            assertEquals(3L, stats.shots().get(2).id());
            assertEquals(4L, stats.shots().get(3).id());
            assertEquals(5L, stats.shots().get(4).id());
        }
    }

    @Nested
    @DisplayName("Edge Case and Statistical Tests")
    class EdgeCaseAndStatisticalTests {

        @Test
        @DisplayName("Should handle single shot statistics")
        void shouldHandleSingleShotStatistics() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var singleShot = List.of(
                new Shot(1L, OWNER_ID, GROUP_ID, getQuantity(2800, feetPerSecond))
            );
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, feetPerSecond),
                getQuantity(0, feetPerSecond),
                getQuantity(0, feetPerSecond),
                singleShot
            );
            
            assertEquals(1, stats.shots().size());
            assertEquals(0, stats.standardDeviation().getValue().doubleValue(), 0.001);
            assertEquals(0, stats.extremeSpread().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle large number of shots")
        void shouldHandleLargeNumberOfShots() {
            @SuppressWarnings("unchecked")
            var fps = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var manyShots = new ArrayList<Shot>();
            for (int i = 0; i < 100; i++) {
                manyShots.add(new Shot((long) i, OWNER_ID, GROUP_ID, 
                    getQuantity(2800 + (i % 20), fps)));
            }
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2810, fps),
                getQuantity(5.77, fps),
                getQuantity(19, fps),
                manyShots
            );
            
            assertEquals(100, stats.shots().size());
        }

        @Test
        @DisplayName("Should handle high standard deviation (poor load)")
        void shouldHandleHighStandardDeviation() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, feetPerSecond),
                getQuantity(50, feetPerSecond),  // poor SD
                getQuantity(150, feetPerSecond), // large ES
                createTestShots()
            );
            
            assertEquals(50, stats.standardDeviation().getValue().doubleValue(), 0.01);
            assertEquals(150, stats.extremeSpread().getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should handle excellent standard deviation (match load)")
        void shouldHandleExcellentStandardDeviation() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800, feetPerSecond),
                getQuantity(3, feetPerSecond),   // excellent SD
                getQuantity(8, feetPerSecond),   // tight ES
                createTestShots()
            );
            
            assertEquals(3, stats.standardDeviation().getValue().doubleValue(), 0.01);
            assertTrue(stats.standardDeviation().getValue().doubleValue() < 5, 
                "SD should be less than 5 for match-grade loads");
        }

        @Test
        @DisplayName("Should handle high precision statistical values")
        void shouldHandleHighPrecisionStatisticalValues() {
            @SuppressWarnings("unchecked")
            var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
            
            var stats = new GroupStatistics(
                createTestGroup(),
                getQuantity(2800.1234, feetPerSecond),
                getQuantity(7.9876, feetPerSecond),
                getQuantity(20.4567, feetPerSecond),
                createTestShots()
            );
            
            assertEquals(2800.1234, stats.averageVelocity().getValue().doubleValue(), 0.0001);
            assertEquals(7.9876, stats.standardDeviation().getValue().doubleValue(), 0.0001);
            assertEquals(20.4567, stats.extremeSpread().getValue().doubleValue(), 0.0001);
        }

        @Test
        @DisplayName("Should maintain extreme spread >= standard deviation invariant")
        void shouldMaintainExtremeSpreadGreaterThanOrEqualToStandardDeviation() {
            var stats = createValidStatistics();
            
            // In real ballistics, ES should typically be >= SD
            // This is a mathematical expectation for most distributions
            assertTrue(stats.extremeSpread().getValue().doubleValue() >= 
                       stats.standardDeviation().getValue().doubleValue(), 
                "Extreme spread should generally be >= standard deviation for ballistic data");
        }
    }
}

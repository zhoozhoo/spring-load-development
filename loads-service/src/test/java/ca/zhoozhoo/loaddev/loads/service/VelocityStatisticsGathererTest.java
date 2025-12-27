package ca.zhoozhoo.loaddev.loads.service;

import static ca.zhoozhoo.loaddev.loads.service.VelocityStatisticsGatherer.compute;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.zhoozhoo.loaddev.loads.service.VelocityStatisticsGatherer.VelocityStats;

/**
 * Unit tests for the {@link VelocityStatisticsGatherer}.
 * <p>
 * Tests the single-pass statistics computation,
 * including average, standard deviation, and extreme spread calculations
 * with proper unit handling.
 * </p>
 *
 * @author Zhubin Salehi
 */
@DisplayName("VelocityStatisticsGatherer Tests")
class VelocityStatisticsGathererTest {

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> FEET_PER_SECOND = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> METRES_PER_SECOND = (Unit<Speed>) METRE.divide(SECOND);

    @Nested
    @DisplayName("Empty Statistics Tests")
    class EmptyStatisticsTests {

        @Test
        @DisplayName("Should create empty stats with all zero values and correct unit")
        void shouldCreateEmptyStatsWithAllZeroValuesAndCorrectUnit() {
            // When
            var stats = VelocityStats.empty(FEET_PER_SECOND);

            // Then
            assertEquals(0, stats.count());
            assertEquals(0.0, stats.sum());
            assertEquals(FEET_PER_SECOND, stats.unit());
            assertEquals(0.0, stats.average().getValue().doubleValue());
            assertEquals(FEET_PER_SECOND, stats.average().getUnit());
            assertEquals(0.0, stats.standardDeviation().getValue().doubleValue());
            assertEquals(FEET_PER_SECOND, stats.standardDeviation().getUnit());
            assertEquals(0.0, stats.extremeSpread().getValue().doubleValue());
            assertEquals(FEET_PER_SECOND, stats.extremeSpread().getUnit());
        }

        @Test
        @DisplayName("Should compute empty statistics for empty list")
        void shouldComputeEmptyStatisticsForEmptyList() {
            // When
            var stats = compute(of(), FEET_PER_SECOND);

            // Then
            assertEquals(0, stats.count());
            assertEquals(0.0, stats.average().getValue().doubleValue());
        }
    }

    @Nested
    @DisplayName("Single Value Tests")
    class SingleValueTests {

        @Test
        @DisplayName("Should compute correct stats for single velocity")
        void shouldComputeCorrectStatsForSingleVelocity() {
            // When
            var stats = compute(of(getQuantity(2800.0, FEET_PER_SECOND)), FEET_PER_SECOND);

            // Then
            assertEquals(1, stats.count());
            assertEquals(2800.0, stats.average().getValue().doubleValue(), 0.001);
            assertEquals(0.0, stats.standardDeviation().getValue().doubleValue());
            assertEquals(0.0, stats.extremeSpread().getValue().doubleValue());
        }

        @Test
        @DisplayName("Should preserve unit for single velocity")
        void shouldPreserveUnitForSingleVelocity() {
            // When
            var stats = compute(of(getQuantity(850.0, METRES_PER_SECOND)), METRES_PER_SECOND);

            // Then
            assertEquals(METRES_PER_SECOND, stats.average().getUnit());
            assertEquals(METRES_PER_SECOND, stats.standardDeviation().getUnit());
            assertEquals(METRES_PER_SECOND, stats.extremeSpread().getUnit());
        }
    }

    @Nested
    @DisplayName("Multiple Values Tests")
    class MultipleValuesTests {

        @Test
        @DisplayName("Should compute correct average for multiple velocities")
        void shouldComputeCorrectAverageForMultipleVelocities() {
            // When
            var stats = compute(of(
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(2810.0, FEET_PER_SECOND),
                getQuantity(2790.0, FEET_PER_SECOND),
                getQuantity(2805.0, FEET_PER_SECOND),
                getQuantity(2795.0, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then
            assertEquals(5, stats.count());
            assertEquals(2800.0, stats.average().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should compute correct standard deviation")
        void shouldComputeCorrectStandardDeviation() {
            // When
            var stats = compute(of(
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(2810.0, FEET_PER_SECOND),
                getQuantity(2790.0, FEET_PER_SECOND),
                getQuantity(2805.0, FEET_PER_SECOND),
                getQuantity(2795.0, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then - expected std dev is approximately 7.07
            assertTrue(stats.standardDeviation().getValue().doubleValue() > 7.0 
                && stats.standardDeviation().getValue().doubleValue() < 8.0, 
                "Standard deviation should be ~7.07, was: " + stats.standardDeviation().getValue().doubleValue());
        }

        @Test
        @DisplayName("Should compute correct extreme spread")
        void shouldComputeCorrectExtremeSpread() {
            // When
            var stats = compute(of(
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(2810.0, FEET_PER_SECOND),
                getQuantity(2790.0, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then
            assertEquals(20.0, stats.extremeSpread().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle velocities with identical values")
        void shouldHandleVelocitiesWithIdenticalValues() {
            // When
            var stats = compute(of(
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(2800.0, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then
            assertEquals(3, stats.count());
            assertEquals(2800.0, stats.average().getValue().doubleValue(), 0.001);
            assertEquals(0.0, stats.standardDeviation().getValue().doubleValue());
            assertEquals(0.0, stats.extremeSpread().getValue().doubleValue());
        }
    }

    @Nested
    @DisplayName("Unit Conversion Tests")
    class UnitConversionTests {

        @Test
        @DisplayName("Should convert mixed units to target unit")
        void shouldConvertMixedUnitsToTargetUnit() {
            // When - mix of fps and m/s
            var stats = compute(of(
                getQuantity(853.44, METRES_PER_SECOND),  // ~2800 fps
                getQuantity(856.49, METRES_PER_SECOND),  // ~2810 fps
                getQuantity(850.39, METRES_PER_SECOND)   // ~2790 fps
            ), METRES_PER_SECOND);

            // Then
            assertEquals(3, stats.count());
            assertEquals(853.44, stats.average().getValue().doubleValue(), 0.5);
            assertEquals(METRES_PER_SECOND, stats.average().getUnit());
        }

        @Test
        @DisplayName("Should handle conversion from fps to mps")
        void shouldHandleConversionFromFpsToMps() {
            // When - velocities in fps, compute in meters per second
            var stats = compute(of(
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(2810.0, FEET_PER_SECOND),
                getQuantity(2790.0, FEET_PER_SECOND)
            ), METRES_PER_SECOND);

            // Then - ~2800 fps = ~853.44 m/s
            assertTrue(stats.average().getValue().doubleValue() > 850 
                && stats.average().getValue().doubleValue() < 856, 
                "Average should be ~853 m/s, was: " + stats.average().getValue().doubleValue());
            assertEquals(METRES_PER_SECOND, stats.average().getUnit());
        }

        @Test
        @DisplayName("Should maintain precision during unit conversion")
        void shouldMaintainPrecisionDuringUnitConversion() {
            // When - convert to fps and back
            var stats = compute(of(getQuantity(853.44, METRES_PER_SECOND)), FEET_PER_SECOND);

            // Then - should be close to 2800 fps
            assertTrue(stats.average().getValue().doubleValue() > 2799 
                && stats.average().getValue().doubleValue() < 2801, 
                "Should be ~2800 fps, was: " + stats.average().getValue().doubleValue());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very small velocities")
        void shouldHandleVerySmallVelocities() {
            // When
            var stats = compute(of(
                getQuantity(500.0, FEET_PER_SECOND),
                getQuantity(501.0, FEET_PER_SECOND),
                getQuantity(499.0, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then
            assertEquals(500.0, stats.average().getValue().doubleValue(), 0.001);
            assertEquals(2.0, stats.extremeSpread().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle very large velocities")
        void shouldHandleVeryLargeVelocities() {
            // When
            var stats = compute(of(
                getQuantity(5000.0, FEET_PER_SECOND),
                getQuantity(4999.0, FEET_PER_SECOND),
                getQuantity(5001.0, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then
            assertEquals(5000.0, stats.average().getValue().doubleValue(), 0.001);
            assertEquals(2.0, stats.extremeSpread().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should handle large dataset")
        void shouldHandleLargeDataset() {
            // When - 100 velocities around 2800 fps
            @SuppressWarnings("unchecked")
            var stats = compute((List<Quantity<Speed>>) (List<?>) java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> getQuantity(2800.0 + (i % 10), FEET_PER_SECOND))
                .toList(), FEET_PER_SECOND);

            // Then
            assertEquals(100, stats.count());
            assertTrue(stats.average().getValue().doubleValue() > 2803 
                && stats.average().getValue().doubleValue() < 2806, 
                "Average should be ~2804.5, was: " + stats.average().getValue().doubleValue());
        }

        @Test
        @DisplayName("Should avoid negative variance from rounding errors")
        void shouldAvoidNegativeVarianceFromRoundingErrors() {
            // When
            var stats = compute(of(
                getQuantity(2800.1, FEET_PER_SECOND),
                getQuantity(2800.2, FEET_PER_SECOND),
                getQuantity(2800.3, FEET_PER_SECOND)
            ), FEET_PER_SECOND);

            // Then - standard deviation should never be negative
            assertTrue(stats.standardDeviation().getValue().doubleValue() >= 0.0, 
                "Standard deviation should never be negative, was: " + stats.standardDeviation().getValue().doubleValue());
        }
    }

    @Nested
    @DisplayName("Accumulator Tests")
    class AccumulatorTests {

        @Test
        @DisplayName("Should accumulate values correctly with add method")
        void shouldAccumulateValuesCorrectlyWithAddMethod() {
            // When
            var stats = VelocityStats.empty(FEET_PER_SECOND)
                .add(getQuantity(2800.0, FEET_PER_SECOND))
                .add(getQuantity(2810.0, FEET_PER_SECOND))
                .add(getQuantity(2790.0, FEET_PER_SECOND));

            // Then
            assertEquals(3, stats.count());
            assertEquals(2800.0, stats.average().getValue().doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should maintain immutability during accumulation")
        void shouldMaintainImmutabilityDuringAccumulation() {
            // Given
            var original = VelocityStats.empty(FEET_PER_SECOND);

            // When
            var modified = original.add(getQuantity(2800.0, FEET_PER_SECOND));

            // Then
            assertEquals(0, original.count(), "Original should remain unchanged");
            assertEquals(1, modified.count(), "Modified should have one value");
        }
    }
}

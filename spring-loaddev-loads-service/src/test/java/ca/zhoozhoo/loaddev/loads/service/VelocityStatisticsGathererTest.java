package ca.zhoozhoo.loaddev.loads.service;

import static ca.zhoozhoo.loaddev.loads.service.VelocityStatisticsGatherer.VelocityStats;
import static ca.zhoozhoo.loaddev.loads.service.VelocityStatisticsGatherer.compute;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for VelocityStatisticsGatherer.
 * Tests statistical calculations for velocity data.
 *
 * @author Zhubin Salehi
 */
class VelocityStatisticsGathererTest {

    @Test
    void empty_shouldReturnEmptyStats() {
        var stats = VelocityStats.empty();
        
        assertEquals(0, stats.count());
        assertEquals(0.0, stats.sum());
        assertEquals(Double.MAX_VALUE, stats.min());
        assertEquals(Double.MIN_VALUE, stats.max());
        assertEquals(0.0, stats.sumOfSquares());
    }

    @Test
    void add_shouldAccumulateValues() {
        var stats = VelocityStats.empty()
                .add(100.0)
                .add(200.0)
                .add(150.0);
        
        assertEquals(3, stats.count());
        assertEquals(450.0, stats.sum());
        assertEquals(100.0, stats.min());
        assertEquals(200.0, stats.max());
    }

    @Test
    void average_withValues_shouldCalculateCorrectly() {
        var stats = VelocityStats.empty()
                .add(100.0)
                .add(200.0)
                .add(150.0);
        
        assertEquals(150.0, stats.average(), 0.001);
    }

    @Test
    void average_withNoValues_shouldReturnZero() {
        assertEquals(0.0, VelocityStats.empty().average());
    }

    @Test
    void standardDeviation_withValues_shouldCalculateCorrectly() {
        var stats = VelocityStats.empty()
                .add(100.0)
                .add(200.0)
                .add(150.0);
        
        // Expected standard deviation for [100, 200, 150]
        // Mean = 150, variance = ((100-150)^2 + (200-150)^2 + (150-150)^2) / 3
        // = (2500 + 2500 + 0) / 3 = 1666.67
        // std dev = sqrt(1666.67) ≈ 40.82
        assertEquals(40.82, stats.standardDeviation(), 0.1);
    }

    @Test
    void standardDeviation_withNoValues_shouldReturnZero() {
        assertEquals(0.0, VelocityStats.empty().standardDeviation());
    }

    @Test
    void extremeSpread_withValues_shouldCalculateCorrectly() {
        var stats = VelocityStats.empty()
                .add(100.0)
                .add(200.0)
                .add(150.0);
        
        assertEquals(100.0, stats.extremeSpread());
    }

    @Test
    void extremeSpread_withNoValues_shouldReturnZero() {
        assertEquals(0.0, VelocityStats.empty().extremeSpread());
    }

    @Test
    void compute_withMultipleValues_shouldCalculateCorrectly() {
        var stats = compute(of(2800, 2850, 2825, 2875, 2810));
        
        assertEquals(5, stats.count());
        assertEquals(2832.0, stats.average(), 1.0);
        assertEquals(75.0, stats.extremeSpread(), 0.001);
    }

    @Test
    void compute_withEmptyList_shouldReturnEmptyStats() {
        var stats = compute(of());
        
        assertEquals(0, stats.count());
        assertEquals(0.0, stats.average());
        assertEquals(0.0, stats.standardDeviation());
        assertEquals(0.0, stats.extremeSpread());
    }

    @Test
    void compute_withSingleValue_shouldHandleCorrectly() {
        var stats = compute(of(2850));
        
        assertEquals(1, stats.count());
        assertEquals(2850.0, stats.average());
        assertEquals(0.0, stats.standardDeviation());
        assertEquals(0.0, stats.extremeSpread());
    }
}

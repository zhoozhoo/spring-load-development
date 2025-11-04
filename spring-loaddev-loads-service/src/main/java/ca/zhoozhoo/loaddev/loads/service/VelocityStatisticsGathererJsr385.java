package ca.zhoozhoo.loaddev.loads.service;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Speed;

import tech.units.indriya.quantity.Quantities;

/**
 * Utility class for computing ballistic statistics in a single pass using JSR-385 Quantity API.
 * <p>
 * This class efficiently computes count, sum, min, max, and sum of squares
 * for velocity measurements using {@link Quantity}&lt;{@link Speed}&gt;, enabling calculation 
 * of average, standard deviation, and extreme spread without multiple iterations over the data.
 * All velocity values maintain their units throughout the computation.
 * </p>
 * <p>
 * This demonstrates Java 25 best practices for efficient data processing using
 * modern record-based accumulator patterns with type-safe unit handling.
 * </p>
 *
 * @author Zhubin Salehi
 */
public class VelocityStatisticsGathererJsr385 {

    /**
     * Statistics accumulator that tracks velocity measurements using JSR-385 Quantity API.
     */
    public record VelocityStats(
            int count,
            double sum,
            double min,
            double max,
            double sumOfSquares,
            Unit<Speed> unit) {

        public static VelocityStats empty(Unit<Speed> unit) {
            return new VelocityStats(0, 0.0, Double.MAX_VALUE, Double.MIN_VALUE, 0.0, unit);
        }

        public VelocityStats add(Quantity<Speed> velocity) {
            // Convert to common unit for arithmetic operations
            var value = velocity.to(unit).getValue().doubleValue();
            return new VelocityStats(
                    count + 1,
                    sum + value,
                    Math.min(min, value),
                    Math.max(max, value),
                    sumOfSquares + (value * value),
                    unit
            );
        }

        public Quantity<Speed> average() {
            return count > 0 ? Quantities.getQuantity(sum / count, unit) : Quantities.getQuantity(0.0, unit);
        }

        public Quantity<Speed> standardDeviation() {
            if (count == 0) return Quantities.getQuantity(0.0, unit);
            var avg = sum / count;
            var variance = (sumOfSquares / count) - (avg * avg);
            return Quantities.getQuantity(Math.sqrt(Math.max(0, variance)), unit);
        }

        public Quantity<Speed> extremeSpread() {
            return count > 0 ? Quantities.getQuantity(max - min, unit) : Quantities.getQuantity(0.0, unit);
        }
    }

    /**
     * Computes statistics from a stream of velocities in a single pass.
     * This is more efficient than making multiple passes over the data.
     *
     * @param velocities iterable of velocity Quantity values
     * @param unit the unit to use for computations (all velocities will be converted to this unit)
     * @return accumulated statistics
     */
    public static VelocityStats compute(Iterable<Quantity<Speed>> velocities, Unit<Speed> unit) {
        var stats = VelocityStats.empty(unit);
        for (var velocity : velocities) {
            stats = stats.add(velocity);
        }
        return stats;
    }
}

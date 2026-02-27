package ca.zhoozhoo.loaddev.loads.service;

import java.util.stream.Gatherer;
import java.util.stream.StreamSupport;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Speed;

import tech.units.indriya.quantity.Quantities;

/// Utility class for computing ballistic statistics in a single pass using
/// Java 25 Stream Gatherers (JEP 485).
///
/// Efficiently computes count, sum, min, max, and sum of squares for velocity
/// measurements using `Quantity<Speed>`, enabling calculation of average,
/// standard deviation, and extreme spread without multiple iterations over the data.
/// All velocity values maintain their units throughout the computation.
///
/// Provides both a Stream Gatherer for composable stream pipelines and a convenience
/// `compute()` method for direct usage.
///
/// @author Zhubin Salehi
public class VelocityStatisticsGatherer {

    /// Statistics accumulator that tracks velocity measurements.
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

    /// Returns a Stream [Gatherer] (JEP 485) that accumulates velocity measurements
    /// into a single `VelocityStats` result in a single pass.
    ///
    /// This enables composable stream pipelines:
    /// ```java
    /// var stats = shots.stream()
    ///     .map(Shot::velocity)
    ///     .gather(VelocityStatisticsGatherer.gatherer(unit))
    ///     .findFirst()
    ///     .orElse(VelocityStats.empty(unit));
    /// ```
    ///
    /// @param unit the unit to use for computations (all velocities will be converted)
    /// @return a Gatherer that produces a single VelocityStats element
    public static Gatherer<Quantity<Speed>, VelocityStats[], VelocityStats> gatherer(Unit<Speed> unit) {
        return Gatherer.ofSequential(
                () -> new VelocityStats[] { VelocityStats.empty(unit) },
                Gatherer.Integrator.ofGreedy((state, velocity, _) -> {
                    state[0] = state[0].add(velocity);
                    return true;
                }),
                (state, downstream) -> downstream.push(state[0])
        );
    }

    /// Computes statistics from an iterable of velocities in a single pass
    /// using the Stream Gatherer internally.
    ///
    /// @param velocities iterable of velocity Quantity values
    /// @param unit the unit to use for computations (all velocities will be converted)
    /// @return accumulated statistics
    public static VelocityStats compute(Iterable<Quantity<Speed>> velocities, Unit<Speed> unit) {
        return StreamSupport.stream(velocities.spliterator(), false)
                .gather(gatherer(unit))
                .findFirst()
                .orElse(VelocityStats.empty(unit));
    }
}

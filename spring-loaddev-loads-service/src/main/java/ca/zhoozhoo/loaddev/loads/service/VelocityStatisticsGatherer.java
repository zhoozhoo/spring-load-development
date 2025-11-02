package ca.zhoozhoo.loaddev.loads.service;

/**
 * Utility class for computing ballistic statistics in a single pass.
 * <p>
 * This class efficiently computes count, sum, min, max, and sum of squares
 * for velocity measurements, enabling calculation of average, standard deviation,
 * and extreme spread without multiple iterations over the data.
 * </p>
 * <p>
 * This demonstrates Java 25 best practices for efficient data processing using
 * modern record-based accumulator patterns.
 * </p>
 *
 * @author Zhubin Salehi
 */
public class VelocityStatisticsGatherer {

    /**
     * Statistics accumulator that tracks velocity measurements.
     */
    public record VelocityStats(
            int count,
            double sum,
            double min,
            double max,
            double sumOfSquares) {

        public static VelocityStats empty() {
            return new VelocityStats(0, 0.0, Double.MAX_VALUE, Double.MIN_VALUE, 0.0);
        }

        public VelocityStats add(double velocity) {
            return new VelocityStats(
                    count + 1,
                    sum + velocity,
                    Math.min(min, velocity),
                    Math.max(max, velocity),
                    sumOfSquares + (velocity * velocity)
            );
        }

        public double average() {
            return count > 0 ? sum / count : 0.0;
        }

        public double standardDeviation() {
            if (count == 0) return 0.0;
            double avg = average();
            double variance = (sumOfSquares / count) - (avg * avg);
            return Math.sqrt(Math.max(0, variance)); // Avoid negative due to rounding
        }

        public double extremeSpread() {
            return count > 0 ? max - min : 0.0;
        }
    }

    /**
     * Computes statistics from a stream of velocities in a single pass.
     * This is more efficient than the original implementation which made 4 separate passes.
     *
     * @param velocities iterable of velocity values
     * @return accumulated statistics
     */
    public static VelocityStats compute(Iterable<Integer> velocities) {
        VelocityStats stats = VelocityStats.empty();
        for (Integer velocity : velocities) {
            stats = stats.add(velocity.doubleValue());
        }
        return stats;
    }
}

package ca.zhoozhoo.loaddev.loads.model;

import static java.lang.Math.round;

import java.util.List;

/**
 * Represents calculated ballistic statistics for a shooting group.
 * <p>
 * This record encapsulates the statistical analysis of shot velocity data for a group,
 * including average velocity, standard deviation, and extreme spread (ES). These metrics
 * are essential for evaluating load consistency and performance. All numeric values are
 * automatically rounded to one decimal place for presentation.
 * </p>
 *
 * @author Zhubin Salehi
 */
public record GroupStatistics(
        Group group,

        double averageVelocity,

        double standardDeviation,

        double extremeSpread,

        List<Shot> shots) {

    // Custom constructor that rounds double values to one decimal point
    public GroupStatistics {
        averageVelocity = round(averageVelocity * 10.0) / 10.0;
        standardDeviation = round(standardDeviation * 10.0) / 10.0;
        extremeSpread = round(extremeSpread * 10.0) / 10.0;
    }
}

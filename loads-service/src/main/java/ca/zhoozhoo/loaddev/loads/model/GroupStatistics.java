package ca.zhoozhoo.loaddev.loads.model;

import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

/**
 * Represents calculated ballistic statistics for a shooting group.
 * <p>
 * This record encapsulates the statistical analysis of shot velocity data for a group,
 * including average velocity, standard deviation, and extreme spread (ES) using javax.measure
 * Quantity API. These metrics are essential for evaluating load consistency and performance.
 * Velocity values are stored as Quantity&lt;Speed&gt; to support multiple unit systems.
 * </p>
 *
 * @author Zhubin Salehi
 */
public record GroupStatistics(
        Group group,

        Quantity<Speed> averageVelocity,

        Quantity<Speed> standardDeviation,

        Quantity<Speed> extremeSpread,

        List<Shot> shots) {

    /**
     * Compact constructor that creates defensive copies of mutable collections.
     * <p>
     * Note: Quantity objects are immutable, so they don't need defensive copying.
     * The shots list is copied to prevent external modification.
     * </p>
     */
    public GroupStatistics {
        shots = shots != null ? List.copyOf(shots) : List.of();
    }
}

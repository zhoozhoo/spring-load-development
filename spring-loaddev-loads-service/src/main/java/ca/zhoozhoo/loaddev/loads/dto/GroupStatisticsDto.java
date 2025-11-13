package ca.zhoozhoo.loaddev.loads.dto;

import java.time.LocalDate;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;

/**
 * Data Transfer Object for group statistics.
 * <p>
 * This DTO provides a simplified view of shooting group statistics for API responses,
 * containing essential group information along with calculated ballistic metrics.
 * All measurements use {@link Quantity} objects to support multiple unit systems
 * (imperial and metric). It excludes internal identifiers and sensitive data while
 * providing all necessary information for client applications to display load
 * performance data with proper unit handling.
 * </p>
 *
 * @author Zhubin Salehi
 */
public record GroupStatisticsDto(

        LocalDate date,

        Quantity<Mass> powderCharge,

        Quantity<Length> targetRange,

        Quantity<Length> groupSize,

        Quantity<Speed> averageVelocity,

        Quantity<Speed> standardDeviation,

        Quantity<Speed> extremeSpread,

        List<ShotDto> shots) {

    /**
     * Compact constructor that creates defensive copies of mutable collections.
     * <p>
     * Note: Quantity objects are immutable, so they don't need defensive copying.
     * The shots list is copied to prevent external modification.
     * </p>
     */
    public GroupStatisticsDto {
        shots = shots != null ? List.copyOf(shots) : List.of();
    }
}

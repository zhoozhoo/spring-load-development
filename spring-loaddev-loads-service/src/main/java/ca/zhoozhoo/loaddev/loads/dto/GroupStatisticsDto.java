package ca.zhoozhoo.loaddev.loads.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for group statistics.
 * <p>
 * This DTO provides a simplified view of shooting group statistics for API responses,
 * containing essential group information along with calculated ballistic metrics.
 * It excludes internal identifiers and sensitive data while providing all necessary
 * information for client applications to display load performance data.
 * </p>
 *
 * @author Zhubin Salehi
 */
public record GroupStatisticsDto(

        LocalDate date,

        Double powderCharge,

        Integer targetRange,

        Double groupSize,

        double averageVelocity,

        double standardDeviation,

        double extremeSpread,

        List<ShotDto> shots) {

    /**
     * Compact constructor that creates defensive copies of mutable collections.
     */
    public GroupStatisticsDto {
        shots = shots != null ? List.copyOf(shots) : List.of();
    }
}

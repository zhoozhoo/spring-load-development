package ca.zhoozhoo.loaddev.mcp.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object representing a shooting group with ballistic statistics.
 *
 * <p>A group represents multiple shots fired at a target under consistent conditions.
 * This record captures both the shooting conditions (date, powder charge, target range)
 * and the resulting ballistic performance metrics (group size, velocity statistics).
 *
 * @author Zhubin Salehi
 */
public record GroupDto(

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
    public GroupDto {
        shots = shots != null ? List.copyOf(shots) : List.of();
    }
}

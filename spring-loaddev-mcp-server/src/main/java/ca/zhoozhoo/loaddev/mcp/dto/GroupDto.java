package ca.zhoozhoo.loaddev.mcp.dto;

import java.time.LocalDate;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;

/**
 * Data Transfer Object representing a shooting group with ballistic statistics.
 * <p>
 * Updated to use JSR-385 {@link Quantity} types for powder charge, target range,
 * group size, and velocity statistics, matching loads-service GroupStatisticsDto.
 */
public record GroupDto(

        LocalDate date,

        Quantity<Mass> powderCharge,

        Quantity<Length> targetRange,

        Quantity<Length> groupSize,

        Quantity<Speed> averageVelocity,

        Quantity<Speed> standardDeviation,

        Quantity<Speed> extremeSpread,

        List<ShotDto> shots) {

    public GroupDto {
        shots = shots != null ? List.copyOf(shots) : List.of();
    }
}

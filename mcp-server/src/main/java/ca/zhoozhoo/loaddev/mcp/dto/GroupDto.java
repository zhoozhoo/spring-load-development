package ca.zhoozhoo.loaddev.mcp.dto;

import java.time.LocalDate;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;

/**
 * Shooting group with ballistic statistics using JSR-385 {@link Quantity} types.
 *
 * @author Zhubin Salehi
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

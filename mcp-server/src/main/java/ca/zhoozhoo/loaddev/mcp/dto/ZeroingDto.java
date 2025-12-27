package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Rifle zeroing configuration using JSR-385 {@link Quantity} types.
 * <p>
 * Units are embedded in Quantity instances for type-safe measurements.
 *
 * @param sightHeight height of sight above bore centerline
 * @param zeroDistance distance at which rifle is zeroed
 *
 * @author Zhubin Salehi
 */
public record ZeroingDto(

        Quantity<Length> sightHeight,

        Quantity<Length> zeroDistance) {
}

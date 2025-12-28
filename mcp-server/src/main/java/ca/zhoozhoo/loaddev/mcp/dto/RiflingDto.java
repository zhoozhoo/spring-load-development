package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Barrel rifling specifications using JSR-385 {@link Quantity} types.
 * <p>
 * Units are embedded in Quantity instances for type-safe measurements.
 *
 * @param twistRate       distance for one complete rotation (twist rate)
 * @param twistDirection  direction of rifling twist (e.g., "RIGHT", "LEFT")
 * @param numberOfGrooves number of grooves cut into the barrel (typically 3, 4, 5, or 6)
 *
 * @author Zhubin Salehi
 */
public record RiflingDto(

        Quantity<Length> twistRate,

        String twistDirection,

        Integer numberOfGrooves) {
}

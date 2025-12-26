package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Rifle specifications using JSR-385 {@link Quantity} types.
 * <p>
 * Units are embedded in Quantity instances for type-safe measurements.
 *
 * @author Zhubin Salehi
 */
public record RifleDto(

        Long id,

        String name,

        String description,

        String caliber,

        Quantity<Length> barrelLength,

        String barrelContour,

        RiflingDto rifling,

        ZeroingDto zeroing) {
}

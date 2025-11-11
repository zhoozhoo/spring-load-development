package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Data Transfer Object representing a rifle aligned with JSR-385.
 * <p>
 * Mirrors the rifles microservice record replacing primitive numeric and string length
 * representations with {@link Quantity} types (barrelLength, freeBore). The previous
 * measurementUnits field is removed; units are embedded in each Quantity instance.
 * </p>
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

        String twistRate,

        String rifling,

        Quantity<Length> freeBore) {
}

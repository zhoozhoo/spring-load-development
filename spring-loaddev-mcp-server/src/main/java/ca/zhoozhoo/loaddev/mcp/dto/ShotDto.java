package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

/**
 * Data Transfer Object representing a single shot measurement.
 * <p>
 * Updated to use JSR-385 {@link Quantity}&lt;{@link Speed}&gt; for unit-aware velocity values.
 */
public record ShotDto(

        Quantity<Speed> velocity) {
}

package ca.zhoozhoo.loaddev.loads.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

/**
 * Data Transfer Object for shot velocity data using JSR-385 Quantity API.
 * <p>
 * This lightweight DTO represents a single shot's velocity measurement using
 * {@link Quantity}&lt;{@link Speed}&gt;, allowing for unit-aware velocity data.
 * Used primarily in group statistics responses to provide shot-level detail
 * without exposing internal identifiers or relationships.
 * </p>
 *
 * @author Zhubin Salehi
 */
public record ShotJsr385Dto(

        Quantity<Speed> velocity) {
}

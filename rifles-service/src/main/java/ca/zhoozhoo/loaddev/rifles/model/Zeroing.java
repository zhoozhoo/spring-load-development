package ca.zhoozhoo.loaddev.rifles.model;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Rifle zeroing configuration with sight height and zero distance.
 *
 * @param sightHeight height of sight above bore centerline
 * @param distance    distance at which rifle is zeroed
 *
 * @author Zhubin Salehi
 */
public record Zeroing(

        Quantity<Length> sightHeight,

        Quantity<Length> zeroDistance) {
}

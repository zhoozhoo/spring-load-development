package ca.zhoozhoo.loaddev.rifles.model;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Barrel rifling specifications.
 *
 * @param twistRate       distance for one complete rotation (twist rate)
 * @param twistDirection  direction of rifling twist
 * @param numberOfGrooves number of grooves cut into the barrel (typically 3, 4, 5, or 6)
 *
 * @author Zhubin Salehi
 */
public record Rifling(

        Quantity<Length> twistRate,

        TwistDirection twistDirection,

        Integer numberOfGrooves) {
}

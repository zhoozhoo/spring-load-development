package ca.zhoozhoo.loaddev.rifles.model;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

/**
 * Barrel rifling specifications.
 *
 * @param riflingStep    distance for one complete rotation (twist rate)
 * @param twistDirection direction of rifling twist
 *
 * @author Zhubin Salehi
 */
public record Rifling(

        Quantity<Length> twistRate,

        TwistDirection twistDirection) {
}

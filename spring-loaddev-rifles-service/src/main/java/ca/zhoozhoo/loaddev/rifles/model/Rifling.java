package ca.zhoozhoo.loaddev.rifles.model;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

public record Rifling(

        Quantity<Length> riflingStep,
        
        TwistDirection twistDirection) {
}

package ca.zhoozhoo.loaddev.rifles.model;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

public record Zeroing(

        Quantity<Length> sightHeight,
        
        Quantity<Length> distance) {
}

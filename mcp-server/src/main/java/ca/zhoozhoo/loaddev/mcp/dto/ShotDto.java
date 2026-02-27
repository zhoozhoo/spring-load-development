package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

/// Single shot measurement with JSR-385 [Quantity]<[Speed]> velocity.
///
/// @author Zhubin Salehi
public record ShotDto(

        Quantity<Speed> velocity) {
}

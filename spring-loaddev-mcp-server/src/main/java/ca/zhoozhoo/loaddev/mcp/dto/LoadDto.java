package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

/**
 * Reloading load specifications using JSR-385 {@link Quantity} types.
 * <p>
 * Units are embedded in Quantity instances for type-safe measurements.
 *
 * @author Zhubin Salehi
 */
public record LoadDto(

        Long id,

        String name,

        String description,

        String powderManufacturer,

        String powderType,

        String bulletManufacturer,

        String bulletType,

        Quantity<Mass> bulletWeight,

        String primerManufacturer,

        String primerType,

        Quantity<Length> distanceFromLands,

        Quantity<Length> caseOverallLength,

        Quantity<Length> neckTension,

        Long rifleId) {
}
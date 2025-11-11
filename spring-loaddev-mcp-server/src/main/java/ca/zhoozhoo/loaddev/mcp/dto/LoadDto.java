package ca.zhoozhoo.loaddev.mcp.dto;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

/**
 * Data Transfer Object representing a reloading load aligned with JSR-385.
 * <p>
 * Mirrors the load microservice record structure using {@link Quantity} types for
 * bullet weight and cartridge measurements (distance from lands, case overall length,
 * neck tension). Units are embedded in each Quantity instance for type-safe handling
 * without a separate measurementUnits field (removed during JSR-385 migration).
 * </p>
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
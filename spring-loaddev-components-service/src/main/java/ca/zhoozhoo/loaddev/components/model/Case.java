package ca.zhoozhoo.loaddev.components.model;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.money.MonetaryAmount;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Represents a cartridge case component for ammunition reloading using JSR-385 and JSR-354 units.
 * <p>
 * A case defines the brass cartridge specifications including manufacturer, caliber,
 * primer size requirements, and cost as a JSR-354 MonetaryAmount. Cases are the foundation of reloaded
 * ammunition and must match the specific caliber being loaded. The quantity per box is stored using
 * the JSR-385 Units of Measurement API as a dimensionless quantity, and the cost uses the JSR-354
 * Money and Currency API, allowing for type-safe unit conversions, calculations, and currency handling.
 * Each case is owned by a specific user for multi-tenant data isolation.
 * </p>
 * <p>
 * The quantityPerBox and cost are stored in the database as JSONB columns.
 * The JSR-385 Quantity type and JSR-354 MonetaryAmount provide compile-time type safety
 * and runtime unit/currency conversions.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table("cases")
public record Case(
    
        @Id Long id,

        @JsonIgnore @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required") @Column("manufacturer") String manufacturer,

        @NotBlank(message = "Caliber is required") @Column("caliber") String caliber,

        @NotNull(message = "Primer size is required") @Column("primer_size") PrimerSize primerSize,

        @NotNull(message = "Cost is required") @PositiveOrZero(message = "Cost must be non-negative") @Column("cost") MonetaryAmount cost,

        @NotNull(message = "Quantity per box is required") @Positive(message = "Quantity per box must be positive") @Column("quantity_per_box") Quantity<Dimensionless> quantityPerBox) {

    /**
     * Custom equals() excluding ownerId to focus on business equality.
     * Records auto-generate equals() including ALL fields, but ownerId is a
     * database-level concern and shouldn't affect business object equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Case caseItem = (Case) o;
        return Objects.equals(id, caseItem.id) &&
                Objects.equals(manufacturer, caseItem.manufacturer) &&
                Objects.equals(caliber, caseItem.caliber) &&
                primerSize == caseItem.primerSize &&
                Objects.equals(cost, caseItem.cost) &&
                Objects.equals(quantityPerBox, caseItem.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, caliber, primerSize, cost, quantityPerBox);
    }
}

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
 * Represents a primer component for ammunition reloading using JSR-385 and JSR-354 units.
 * <p>
 * A primer provides the ignition source for ammunition. This record defines primer
 * specifications including manufacturer, type (e.g., magnum, standard), size (small
 * rifle, large rifle, etc.), and cost as a JSR-354 MonetaryAmount. Primers must match both the case
 * primer pocket size and the propellant charge requirements. The quantity per box is stored using
 * the JSR-385 Units of Measurement API as a dimensionless quantity, and the cost uses the JSR-354
 * Money and Currency API, allowing for type-safe unit conversions, calculations, and currency handling.
 * Each primer is owned by a specific user for multi-tenant data isolation.
 * </p>
 * <p>
 * The quantityPerBox and cost are stored in the database as JSONB columns.
 * The JSR-385 Quantity type and JSR-354 MonetaryAmount provide compile-time type safety
 * and runtime unit/currency conversions.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table("primers")
public record Primer(
    
        @Id Long id,

        @JsonIgnore @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required") @Column("manufacturer") String manufacturer,

        @NotBlank(message = "Type is required") @Column("type") String type,

        @NotNull(message = "Primer size is required") @Column("size") PrimerSize primerSize,

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
        Primer primer = (Primer) o;
        return Objects.equals(id, primer.id) &&
                Objects.equals(manufacturer, primer.manufacturer) &&
                Objects.equals(type, primer.type) &&
                primerSize == primer.primerSize &&
                Objects.equals(cost, primer.cost) &&
                Objects.equals(quantityPerBox, primer.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, type, primerSize, cost, quantityPerBox);
    }
}

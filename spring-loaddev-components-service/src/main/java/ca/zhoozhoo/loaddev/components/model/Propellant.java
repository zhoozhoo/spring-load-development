package ca.zhoozhoo.loaddev.components.model;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
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
 * Represents a propellant component for ammunition reloading using JSR-385 and JSR-354 units.
 * <p>
 * Propellant (smokeless powder) is the combustible substance that generates gas pressure to propel
 * the projectile. This record defines propellant specifications including manufacturer, type
 * (e.g., IMR 4064, H4350), container weight as a JSR-385 Quantity, and cost as a JSR-354
 * MonetaryAmount. The weight is stored using the JSR-385 Units of Measurement API, and the
 * cost uses the JSR-354 Money and Currency API, allowing for type-safe unit conversions,
 * calculations, and currency handling. Propellant selection is critical for achieving desired
 * velocities and pressures safely. Each propellant is owned by a specific user for
 * multi-tenant data isolation.
 * </p>
 * <p>
 * The weight per container and cost are stored in the database as JSONB columns.
 * The JSR-385 Quantity type and JSR-354 MonetaryAmount provide compile-time type safety
 * and runtime unit/currency conversions.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "propellants")
public record Propellant(
        @Id Long id,

        @JsonIgnore @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required") @Column("manufacturer") String manufacturer,

        @NotBlank(message = "Type is required") @Column("type") String type,

        @NotNull(message = "Cost is required") @PositiveOrZero(message = "Cost must be non-negative") @Column("cost") MonetaryAmount cost,

        @NotNull(message = "Weight per container is required") @Positive(message = "Weight per container must be positive") @Column("weight_per_container") Quantity<Mass> weightPerContainer) {

    /**
     * Custom equals() excluding ownerId to focus on business equality.
     * Records auto-generate equals() including ALL fields, but ownerId is a
     * database-level concern and shouldn't affect business object equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Propellant propellant = (Propellant) o;
        return Objects.equals(id, propellant.id) &&
                Objects.equals(manufacturer, propellant.manufacturer) &&
                Objects.equals(type, propellant.type) &&
                Objects.equals(cost, propellant.cost) &&
                Objects.equals(weightPerContainer, propellant.weightPerContainer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, type, cost, weightPerContainer);
    }
}

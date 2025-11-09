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
 * Represents a projectile component for ammunition reloading using JSR-385 and JSR-354 units.
 * <p>
 * A projectile defines the bullet specifications including manufacturer, weight as a JSR-385 Quantity,
 * type, and cost as a JSR-354 MonetaryAmount. The weight is stored using the JSR-385 Units of
 * Measurement API, and the cost uses the JSR-354 Money and Currency API, allowing for type-safe
 * unit conversions, calculations, and currency handling.
 * Each projectile is owned by a specific user for multi-tenant data isolation.
 * </p>
 * <p>
 * The weight and cost are stored in the database as JSONB columns.
 * The JSR-385 Quantity type and JSR-354 MonetaryAmount provide compile-time type safety
 * and runtime unit/currency conversions.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "projectiles")
public record Projectile(

        @Id Long id,

        @JsonIgnore @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required") @Column("manufacturer") String manufacturer,

        @NotNull(message = "Weight is required") @Positive(message = "Weight must be positive") @Column("weight") Quantity<Mass> weight,

        @NotBlank(message = "Type is required") @Column("type") String type,

        @NotNull(message = "Cost is required") @PositiveOrZero(message = "Cost must be non-negative") @Column("cost") MonetaryAmount cost,

        @NotNull(message = "Quantity per box is required") @Positive(message = "Quantity per box must be positive") @Column Integer quantityPerBox) {

    /**
     * Custom equals() excluding ownerId to focus on business equality.
     * Records auto-generate equals() including ALL fields, but ownerId is a
     * database-level concern and shouldn't affect business object equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Projectile projectile = (Projectile) o;
        return Objects.equals(id, projectile.id) &&
                Objects.equals(manufacturer, projectile.manufacturer) &&
                Objects.equals(weight, projectile.weight) &&
                Objects.equals(type, projectile.type) &&
                Objects.equals(cost, projectile.cost) &&
                Objects.equals(quantityPerBox, projectile.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, weight, type,
                cost, quantityPerBox);
    }
}

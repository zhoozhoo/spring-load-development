package ca.zhoozhoo.loaddev.components.model;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.money.MonetaryAmount;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.zhoozhoo.loaddev.common.jackson.MonetaryAmountDeserializer;
import ca.zhoozhoo.loaddev.common.jackson.MonetaryAmountSerializer;
import ca.zhoozhoo.loaddev.common.jackson.QuantityDeserializer;
import ca.zhoozhoo.loaddev.common.jackson.QuantitySerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Projectile component with JSR-385 Quantity and JSR-354 MonetaryAmount.
 * <p>
 * Stores weight and cost as PostgreSQL JSONB for type-safe calculations.
 * Multi-tenant by ownerId.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "projectiles")
public record Projectile (

        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required")
        @Column("manufacturer") String manufacturer,

        @JsonSerialize(using = QuantitySerializer.class)
        @JsonDeserialize(using = QuantityDeserializer.class)
        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be positive")
        @Column("weight") Quantity<Mass> weight,

        @NotBlank(message = "Type is required")
        @Column("type") String type,

        @JsonSerialize(using = MonetaryAmountSerializer.class)
        @JsonDeserialize(using = MonetaryAmountDeserializer.class)
        @NotNull(message = "Cost is required")
        @PositiveOrZero(message = "Cost must be non-negative")
        @Column("cost") MonetaryAmount cost,

        @NotNull(message = "Quantity per box is required")
        @Positive(message = "Quantity per box must be positive")
        @Column("quantity_per_box") Integer quantityPerBox) implements Component {

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

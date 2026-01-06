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
 * Propellant component with JSR-385 Quantity and JSR-354 MonetaryAmount.
 * <p>
 * Stores weightPerContainer and cost as PostgreSQL JSONB for type-safe calculations.
 * Multi-tenant by ownerId.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "propellants")
public record Propellant(
        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required")
        @Column("manufacturer") String manufacturer,

        @NotBlank(message = "Type is required")
        @Column("type") String type,

        @JsonSerialize(using = MonetaryAmountSerializer.class)
        @JsonDeserialize(using = MonetaryAmountDeserializer.class)
        @NotNull(message = "Cost is required")
        @PositiveOrZero(message = "Cost must be non-negative")
        @Column("cost") MonetaryAmount cost,

        @JsonSerialize(using = QuantitySerializer.class)
        @JsonDeserialize(using = QuantityDeserializer.class)
        @NotNull(message = "Weight per container is required")
        @Positive(message = "Weight per container must be positive")
        @Column("weight_per_container") Quantity<Mass> weightPerContainer) implements Component {

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

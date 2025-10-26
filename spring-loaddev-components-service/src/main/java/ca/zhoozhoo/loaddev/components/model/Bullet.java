package ca.zhoozhoo.loaddev.components.model;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Represents a bullet component for ammunition reloading.
 * <p>
 * A bullet defines the projectile specifications including manufacturer, weight, type,
 * and cost information. Bullets can be measured in either metric or imperial units.
 * Each bullet is owned by a specific user for multi-tenant data isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "bullets")
public record Bullet(

        @Id Long id,

        @JsonIgnore @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required") @Column("manufacturer") String manufacturer,

        @NotNull(message = "Weight is required") @Positive(message = "Weight must be positive") @Column("weight") Double weight,

        @NotBlank(message = "Type is required") @Column("type") String type,

        @NotBlank(message = "Measurement Units is required") @Column("measurement_units") String measurementUnits,

        @NotNull(message = "Cost is required") @Positive(message = "Cost must be positive") @Column BigDecimal cost,

        @NotNull(message = "Currency is required") @NotBlank(message = "Currency is required") @Column String currency,

        @NotNull(message = "Quantity per box is required") @Positive(message = "Quantity per box must be positive") @Column Integer quantityPerBox) {

    public static final String METRIC = "Metric";

    public static final String IMPERIAL = "Imperial";

    /**
     * Custom equals() excluding ownerId to focus on business equality.
     * Records auto-generate equals() including ALL fields, but ownerId is a
     * database-level concern and shouldn't affect business object equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bullet bullet = (Bullet) o;
        return Objects.equals(id, bullet.id) &&
                Objects.equals(manufacturer, bullet.manufacturer) &&
                Objects.equals(weight, bullet.weight) &&
                Objects.equals(type, bullet.type) &&
                Objects.equals(measurementUnits, bullet.measurementUnits) &&
                Objects.equals(cost, bullet.cost) &&
                Objects.equals(currency, bullet.currency) &&
                Objects.equals(quantityPerBox, bullet.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, weight, type,
                measurementUnits, cost, currency, quantityPerBox);
    }
}

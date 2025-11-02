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
 * Represents a powder component for ammunition reloading.
 * <p>
 * Powder (propellant) is the combustible substance that generates gas pressure to propel
 * the bullet. This record defines powder specifications including manufacturer, type
 * (e.g., IMR 4064, H4350), measurement units (metric or imperial), container weight,
 * and cost information. Powder selection is critical for achieving desired velocities
 * and pressures safely. Each powder is owned by a specific user for multi-tenant data isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "powders")
public record Powder(
        @Id Long id,

        @JsonIgnore @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required") @Column("manufacturer") String manufacturer,

        @NotBlank(message = "Type is required") @Column("type") String type,

        @NotBlank(message = "Measurement Units is required") @Column("measurement_units") String measurementUnits,

        @NotNull(message = "Cost is required") @Positive(message = "Cost must be positive") @Column BigDecimal cost,

        @NotNull(message = "Currency is required") @NotBlank(message = "Currency is required") @Column String currency,

        @NotNull(message = "Weight per container is required") @Positive(message = "Weight per container must be positive") @Column("weight_per_container") Double weightPerContainer) {

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
        Powder powder = (Powder) o;
        return Objects.equals(id, powder.id) &&
                Objects.equals(manufacturer, powder.manufacturer) &&
                Objects.equals(type, powder.type) &&
                Objects.equals(measurementUnits, powder.measurementUnits) &&
                Objects.equals(cost, powder.cost) &&
                Objects.equals(currency, powder.currency) &&
                Objects.equals(weightPerContainer, powder.weightPerContainer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, type, measurementUnits, cost, currency, weightPerContainer);
    }
}

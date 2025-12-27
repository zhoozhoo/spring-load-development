package ca.zhoozhoo.loaddev.components.model;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
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
 * Cartridge case component with JSR-385 Quantity and JSR-354 MonetaryAmount.
 * <p>
 * Stores quantityPerBox and cost as PostgreSQL JSONB for type-safe calculations.
 * Multi-tenant by ownerId.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table("cases")
public record Case(
    
        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotBlank(message = "Manufacturer is required")
        @Column("manufacturer") String manufacturer,

        @NotBlank(message = "Caliber is required")
        @Column("caliber") String caliber,

        @NotNull(message = "Primer size is required")
        @Column("primer_size") PrimerSize primerSize,

        @JsonSerialize(using = MonetaryAmountSerializer.class)
        @JsonDeserialize(using = MonetaryAmountDeserializer.class)
        @NotNull(message = "Cost is required")
        @PositiveOrZero(message = "Cost must be non-negative")
        @Column("cost") MonetaryAmount cost,

        @JsonSerialize(using = QuantitySerializer.class)
        @JsonDeserialize(using = QuantityDeserializer.class)
        @NotNull(message = "Quantity per box is required")
        @Positive(message = "Quantity per box must be positive")
        @Column("quantity_per_box") Quantity<Dimensionless> quantityPerBox) {

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

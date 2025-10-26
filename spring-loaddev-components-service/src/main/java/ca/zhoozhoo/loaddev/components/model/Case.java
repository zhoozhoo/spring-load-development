package ca.zhoozhoo.loaddev.components.model;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a cartridge case component for ammunition reloading.
 * <p>
 * A case defines the brass cartridge specifications including manufacturer, caliber,
 * primer size requirements, and cost information. Cases are the foundation of reloaded
 * ammunition and must match the specific caliber being loaded. Each case is owned by
 * a specific user for multi-tenant data isolation.
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

        @NotNull(message = "Cost is required") @DecimalMin(value = "0.0", message = "Cost must be greater than or equal to 0") @Column("cost") BigDecimal cost,

        @NotBlank(message = "Currency is required") @Column("currency") String currency,

        @NotNull(message = "Quantity per box is required") @Min(value = 1, message = "Quantity per box must be greater than 0") @Column("quantity_per_box") Integer quantityPerBox) {

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
                Objects.equals(currency, caseItem.currency) &&
                Objects.equals(quantityPerBox, caseItem.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, caliber, primerSize, cost, currency, quantityPerBox);
    }
}

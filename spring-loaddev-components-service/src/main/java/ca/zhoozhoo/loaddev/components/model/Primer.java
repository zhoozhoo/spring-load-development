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
 * Represents a primer component for ammunition reloading.
 * <p>
 * A primer provides the ignition source for ammunition. This record defines primer
 * specifications including manufacturer, type (e.g., magnum, standard), size (small
 * rifle, large rifle, etc.), and cost information. Primers must match both the case
 * primer pocket size and the powder charge requirements. Each primer is owned by
 * a specific user for multi-tenant data isolation.
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
        Primer primer = (Primer) o;
        return Objects.equals(id, primer.id) &&
                Objects.equals(manufacturer, primer.manufacturer) &&
                Objects.equals(type, primer.type) &&
                primerSize == primer.primerSize &&
                Objects.equals(cost, primer.cost) &&
                Objects.equals(currency, primer.currency) &&
                Objects.equals(quantityPerBox, primer.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, type, primerSize, cost, currency, quantityPerBox);
    }
}

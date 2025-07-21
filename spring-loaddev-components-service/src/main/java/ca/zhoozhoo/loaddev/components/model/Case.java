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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Case casing = (Case) o;
        return Objects.equals(id, casing.id) &&
                Objects.equals(manufacturer, casing.manufacturer) &&
                Objects.equals(caliber, casing.caliber) &&
                primerSize == casing.primerSize &&
                Objects.equals(cost, casing.cost) &&
                Objects.equals(currency, casing.currency) &&
                Objects.equals(quantityPerBox, casing.quantityPerBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, manufacturer, caliber, primerSize,
                cost, currency, quantityPerBox);
    }
}

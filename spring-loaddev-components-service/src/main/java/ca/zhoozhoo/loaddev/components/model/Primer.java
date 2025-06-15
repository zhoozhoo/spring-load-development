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

    public enum PrimerSize {
        SMALL_PISTOL,
        LARGE_PISTOL,
        SMALL_RIFLE,
        LARGE_RIFLE,
        SMALL_RIFLE_MAGNUM,
        LARGE_RIFLE_MAGNUM,
        SMALL_PISTOL_MAGNUM,
        LARGE_PISTOL_MAGNUM
    }

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
        return Objects.hash(id, manufacturer, type, primerSize,
                cost, currency, quantityPerBox);
    }
}

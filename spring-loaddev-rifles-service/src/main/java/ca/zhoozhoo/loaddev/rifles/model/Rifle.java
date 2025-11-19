package ca.zhoozhoo.loaddev.rifles.model;

import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import ca.zhoozhoo.loaddev.common.jackson.QuantityDeserializer;
import ca.zhoozhoo.loaddev.common.jackson.QuantitySerializer;
import jakarta.validation.constraints.NotBlank;

/**
 * Rifle firearm specifications for ammunition load development.
 * <p>
 * Includes caliber, barrel length, twist rate, and free bore measurements using
 * JSR-385 Quantity types. Each rifle is owned by a specific user for multi-tenant isolation.
 *
 * @author Zhubin Salehi
 */
@Table(name = "rifles")
public record Rifle(

        @Id Long id,

        @Column("owner_id") String ownerId,

        @NotBlank(message = "Name is required")
        @Column("name") String name,

        @Column("description") String description,

        @NotBlank(message = "Caliber is required")
        @Column("caliber") String caliber,

        @JsonSerialize(using = QuantitySerializer.class)
        @JsonDeserialize(using = QuantityDeserializer.class)
        @Column("barrel_length") Quantity<Length> barrelLength,

        @Column("barrel_contour") String barrelContour,

        @Column("twist_rate") String twistRate,

        @Column("rifling") String rifling,

        @JsonSerialize(using = QuantitySerializer.class)
        @JsonDeserialize(using = QuantityDeserializer.class)
        @Column("free_bore") Quantity<Length> freeBore) {

    /**
     * Validates rifle specifications:
     * barrel length (4-50 inches) and free bore (0.001-0.5 inches).
     */
    public Rifle {
        // Validate reasonable barrel length (4 to 50 inches equivalent)
        if (barrelLength != null) {
            double lengthInInches = barrelLength.to(INCH_INTERNATIONAL).getValue().doubleValue();
            if (lengthInInches < 4.0 || lengthInInches > 50.0) {
                throw new IllegalArgumentException(
                    "Barrel length must be between 4.0 and 50.0 inches, got: %.2f".formatted(lengthInInches)
                );
            }
        }
        
        // Validate reasonable free bore (0.001 to 0.5 inches equivalent)
        if (freeBore != null) {
            double freeBoreInInches = freeBore.to(INCH_INTERNATIONAL).getValue().doubleValue();
            if (freeBoreInInches < 0.001 || freeBoreInInches > 0.5) {
                throw new IllegalArgumentException(
                    "Free bore must be between 0.001 and 0.5 inches, got: %.4f".formatted(freeBoreInInches)
                );
            }
        }
    }
}
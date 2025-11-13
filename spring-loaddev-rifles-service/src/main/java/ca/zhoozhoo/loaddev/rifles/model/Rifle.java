package ca.zhoozhoo.loaddev.rifles.model;

import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a rifle firearm for ammunition load development using JSR-385 units.
 * <p>
 * A rifle defines the firearm specifications including name, description, caliber,
 * barrel length, twist rate. These specifications are critical
 * for load development as they affect bullet selection, powder charge, and overall
 * ballistic performance. Each rifle is owned by a specific user for multi-tenant data isolation.
 * Barrel length and free bore use JSR-385 Quantity&lt;Length&gt; for type-safe measurements with embedded units.
 * </p>
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

        @Column("barrel_length") Quantity<Length> barrelLength,

        @Column("barrel_contour") String barrelContour,

        @Column("twist_rate") String twistRate,

        @Column("rifling") String rifling,

        @Column("free_bore") Quantity<Length> freeBore) {

    public static final String METRIC = "Metric";

    public static final String IMPERIAL = "Imperial";

    /**
     * Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
     * <p>
     * Validates reasonable ranges for rifle specifications using JSR-385 Quantity types.
     * Uses enhanced pattern matching for improved readability and maintainability.
     * </p>
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
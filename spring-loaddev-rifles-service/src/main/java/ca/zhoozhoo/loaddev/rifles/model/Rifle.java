package ca.zhoozhoo.loaddev.rifles.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Represents a rifle firearm for ammunition load development.
 * <p>
 * A rifle defines the firearm specifications including name, description, caliber,
 * barrel length, twist rate, and measurement units. These specifications are critical
 * for load development as they affect bullet selection, powder charge, and overall
 * ballistic performance. Each rifle is owned by a specific user for multi-tenant data isolation.
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

        @NotBlank(message = "Measurement Units is required")
        @Column("measurement_units") String measurementUnits,

        @NotBlank(message = "Caliber is required")
        @Column("caliber") String caliber,

        @Positive(message = "Barrel length must be positive")
        @Column("barrel_length") Double barrelLength,

        @Column("barrel_contour") String barrelContour,

        @Column("twist_rate") String twistRate,

        @Column("rifling") String rifling,

        @Positive(message = "Free bore must be positive")
        @Column("free_bore") Double freeBore) {

    public static final String METRIC = "Metric";

    public static final String IMPERIAL = "Imperial";

    /**
     * Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
     * <p>
     * Validates measurement units and reasonable ranges for rifle specifications.
     * Uses enhanced pattern matching for improved readability and maintainability.
     * </p>
     */
    public Rifle {
        // Validate measurement units
        if (measurementUnits != null && !METRIC.equals(measurementUnits) && !IMPERIAL.equals(measurementUnits)) {
            throw new IllegalArgumentException(
                "Measurement units must be either '%s' or '%s'".formatted(METRIC, IMPERIAL)
            );
        }
        
        // Validate reasonable barrel length (4 to 50 inches)
        if (barrelLength instanceof Double length && (length < 4.0 || length > 50.0)) {
            throw new IllegalArgumentException(
                "Barrel length must be between 4.0 and 50.0 inches, got: %.2f".formatted(length)
            );
        }
        
        // Validate reasonable free bore (0.001 to 0.5 inches)
        if (freeBore instanceof Double bore && (bore < 0.001 || bore > 0.5)) {
            throw new IllegalArgumentException(
                "Free bore must be between 0.001 and 0.5 inches, got: %.4f".formatted(bore)
            );
        }
    }
}
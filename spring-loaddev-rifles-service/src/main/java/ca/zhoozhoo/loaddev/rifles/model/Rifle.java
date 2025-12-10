package ca.zhoozhoo.loaddev.rifles.model;

import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import java.util.Objects;

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
 * Includes caliber, barrel length, rifling, and optional zeroing configuration using
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

        @Column("rifling") Rifling rifling,

        @Column("zeroing") Zeroing zeroing) {

    /**
     * Validates rifle specifications: barrel length (4-50 inches).
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Rifle rifle = (Rifle) o;
        return Objects.equals(id, rifle.id) &&
               Objects.equals(ownerId, rifle.ownerId) &&
               Objects.equals(name, rifle.name) &&
               Objects.equals(description, rifle.description) &&
               Objects.equals(caliber, rifle.caliber) &&
               quantitiesEqual(barrelLength, rifle.barrelLength) &&
               Objects.equals(barrelContour, rifle.barrelContour) &&
               Objects.equals(rifling, rifle.rifling) &&
               Objects.equals(zeroing, rifle.zeroing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, name, description, caliber, 
                           quantityHashCode(barrelLength), barrelContour, rifling, zeroing);
    }

    private boolean quantitiesEqual(Quantity<?> q1, Quantity<?> q2) {
        if (q1 == null && q2 == null) return true;
        if (q1 == null || q2 == null) return false;
        // Use Double.compare to avoid floating point equality issues
        return Double.compare(q1.getValue().doubleValue(), q2.getValue().doubleValue()) == 0 &&
               Objects.equals(q1.getUnit(), q2.getUnit());
    }

    private int quantityHashCode(Quantity<?> q) {
        if (q == null) return 0;
        return Objects.hash(q.getValue().doubleValue(), q.getUnit());
    }
}
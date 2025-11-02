package ca.zhoozhoo.loaddev.loads.model;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.format.QuantityFormat;
import javax.measure.quantity.Speed;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.unit.Units;

import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;

/**
 * Represents an individual shot fired as part of a shooting group.
 * <p>
 * A shot records the velocity measurement of a single round fired using javax.measure Quantity API.
 * Multiple shots are grouped together to calculate statistical data such as average velocity,
 * standard deviation, and extreme spread for load performance analysis.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "shots_jsr385")
public record ShotJsr385(

        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotNull(message = "Group ID is required")
        @Column("group_id") Long groupId,

        @Positive(message = "Velocity must be a positive number")
        @Column("velocity") Quantity<Speed> velocity) {

    private static final QuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();

    /**
     * Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
     * <p>
     * Validates that velocity is within reasonable ballistic ranges for small arms ammunition.
     * Converts to feet per second before validation to handle any unit system.
     * </p>
     */
    public ShotJsr385 {
        // Validate reasonable velocity range (500 to 5000 fps)
        // Most rifle rounds fall between 800-3500 fps
        // Convert to feet per second before validation to handle any unit system
        if (velocity != null) {
            // Create feet per second unit (foot_international/second)
            @SuppressWarnings("unchecked")
            Unit<Speed> feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);
            double velocityInFps = velocity.to(feetPerSecond).getValue().doubleValue();
            
            if (velocityInFps < 500 || velocityInFps > 5000) {
                throw new IllegalArgumentException(
                    "Velocity must be between 500 and 5000 fps (feet per second), got: " + QUANTITY_FORMAT.format(velocity)
                );
            }
        }
    }

    /**
     * Custom equals() excluding ownerId to focus on business equality.
     * Records auto-generate equals() including ALL fields, but ownerId is a
     * database-level concern and shouldn't affect business object equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShotJsr385 shot = (ShotJsr385) o;
        return Objects.equals(id, shot.id) &&
                Objects.equals(groupId, shot.groupId) &&
                Objects.equals(velocity, shot.velocity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, velocity);
    }
}

package ca.zhoozhoo.loaddev.loads.model;

import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.format.QuantityFormat;
import javax.measure.quantity.Speed;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.zhoozhoo.loaddev.common.jackson.QuantityDeserializer;
import ca.zhoozhoo.loaddev.common.jackson.QuantitySerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.unit.Units;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/// Represents an individual shot fired as part of a shooting group.
///
/// A shot records the velocity measurement of a single round fired using javax.measure Quantity API.
/// Multiple shots are grouped together to calculate statistical data such as average velocity,
/// standard deviation, and extreme spread for load performance analysis.
///
/// @author Zhubin Salehi
@Table(name = "shots")
public record Shot(

        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotNull(message = "Group ID is required")
        @Column("group_id") Long groupId,

        @JsonSerialize(using = QuantitySerializer.class)
        @JsonDeserialize(using = QuantityDeserializer.class)
        @Positive(message = "Velocity must be a positive number")
        @Column("velocity") Quantity<Speed> velocity) {

    private static final QuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();

    /// Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
    ///
    /// Validates that velocity is within reasonable ballistic ranges for small arms ammunition.
    /// Converts to feet per second before validation to handle any unit system.
    public Shot {
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

    /// Creates a copy of this shot for a new owner, with id set to null.
    public Shot withOwner(String ownerId) {
        return new Shot(null, ownerId, groupId, velocity);
    }

    /// Creates a copy preserving id and ownerId from an existing record.
    public Shot withIdAndOwner(Long id, String ownerId) {
        return new Shot(id, ownerId, groupId, velocity);
    }

    /// Custom equals() excluding ownerId to focus on business equality.
    /// Records auto-generate equals() including ALL fields, but ownerId is a
    /// database-level concern and shouldn't affect business object equality.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shot shot = (Shot) o;
        return Objects.equals(id, shot.id) &&
                Objects.equals(groupId, shot.groupId) &&
                Objects.equals(velocity, shot.velocity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, velocity);
    }
}

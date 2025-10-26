package ca.zhoozhoo.loaddev.loads.model;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Represents a shooting group for a specific load configuration.
 * <p>
 * A group represents a set of shots fired on a specific date using a particular load
 * with a defined powder charge and target distance. The group size (in inches or MOA)
 * measures the accuracy of the load configuration. Each group is associated with a load
 * and owned by a specific user.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "groups")
public record Group(
        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotNull(message = "Load ID is required")
        @Column("load_id") Long loadId,

        @NotNull(message = "Date is required")
        @Column("date") LocalDate date,

        @NotNull(message = "Powder charge is required")
        @Positive(message = "Powder charge must be positive")
        @Column("powder_charge") Double powderCharge,

        @NotNull(message = "Target range is required")
        @Positive(message = "Target range must be positive")
        @Column("target_range") Integer targetRange,

        @Positive(message = "Group size must be positive")
        @Column("group_size") Double groupSize
) {
    /**
     * Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
     * <p>
     * Validates business rules including reasonable ranges for ballistic measurements
     * using Java 25 enhanced instanceof pattern matching for cleaner validation code.
     * </p>
     */
    public Group {
        // Validate date is not in the future
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Group date cannot be in the future");
        }
        
        // Validate reasonable powder charge range (0.1 to 150 grains) using enhanced instanceof
        if (powderCharge instanceof Double charge && (charge < 0.1 || charge > 150.0)) {
            throw new IllegalArgumentException(
                "Powder charge must be between 0.1 and 150.0 grains, got: %.2f".formatted(charge)
            );
        }
        
        // Validate reasonable target range (10 to 2000 yards) using enhanced instanceof
        if (targetRange instanceof Integer range && (range < 10 || range > 2000)) {
            throw new IllegalArgumentException(
                "Target range must be between 10 and 2000 yards, got: %d".formatted(range)
            );
        }
        
        // Validate reasonable group size (0.01 to 50 inches) using enhanced instanceof
        if (groupSize instanceof Double size && (size < 0.01 || size > 50.0)) {
            throw new IllegalArgumentException(
                "Group size must be between 0.01 and 50.0 inches, got: %.3f".formatted(size)
            );
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
        Group group = (Group) o;
        return Objects.equals(id, group.id) &&
                Objects.equals(loadId, group.loadId) &&
                Objects.equals(date, group.date) &&
                Objects.equals(powderCharge, group.powderCharge) &&
                Objects.equals(targetRange, group.targetRange) &&
                Objects.equals(groupSize, group.groupSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, loadId, date, powderCharge, targetRange, groupSize);
    }
}

package ca.zhoozhoo.loaddev.loads.model;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.measure.Quantity;
import javax.measure.format.QuantityFormat;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.units.indriya.format.SimpleQuantityFormat;

import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;

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
@Table(name = "groups_jsr385")
public record GroupJsr385(
        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotNull(message = "Load ID is required")
        @Column("load_id") Long loadId,

        @NotNull(message = "Date is required")
        @Column("date") LocalDate date,

        @NotNull(message = "Powder charge is required")
        @Positive(message = "Powder charge must be positive")
        @Column("powder_charge") Quantity<Mass> powderCharge,

        @NotNull(message = "Target range is required")
        @Positive(message = "Target range must be positive")
        @Column("target_range") Quantity<Length> targetRange,

        @Positive(message = "Group size must be positive")
        @Column("group_size") Quantity<Length> groupSize
) {
    private static final QuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();

    /**
     * Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
     * <p>
     * Validates business rules including reasonable ranges for ballistic measurements
     * using javax.measure Quantity API with proper unit conversions to imperial units
     * (grains, yards, inches) before validation.
     * </p>
     */
    public GroupJsr385 {
        // Validate date is not in the future
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Group date cannot be in the future");
        }
        
        // Validate reasonable powder charge range (0.1 to 150 grains)
        // Convert to grains before validation to handle any unit system
        if (powderCharge != null) {
            double chargeInGrains = powderCharge.to(GRAIN).getValue().doubleValue();
            if (chargeInGrains < 0.1 || chargeInGrains > 150.0) {
                throw new IllegalArgumentException(
                    "Powder charge must be between 0.1 and 150.0 grains, got: " + QUANTITY_FORMAT.format(powderCharge)
                );
            }
        }
        
        // Validate reasonable target range (10 to 2000 yards)
        // Convert to yards before validation to handle any unit system
        if (targetRange != null) {
            double rangeInYards = targetRange.to(YARD_INTERNATIONAL).getValue().doubleValue();
            if (rangeInYards < 10 || rangeInYards > 2000) {
                throw new IllegalArgumentException(
                    "Target range must be between 10 and 2000 yards, got: " + QUANTITY_FORMAT.format(targetRange)
                );
            }
        }
        
        // Validate reasonable group size (0.01 to 50 inches)
        // Convert to inches before validation to handle any unit system
        if (groupSize != null) {
            double sizeInInches = groupSize.to(INCH_INTERNATIONAL).getValue().doubleValue();
            if (sizeInInches < 0.01 || sizeInInches > 50.0) {
                throw new IllegalArgumentException(
                    "Group size must be between 0.01 and 50.0 inches, got: " + QUANTITY_FORMAT.format(groupSize)
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
        GroupJsr385 group = (GroupJsr385) o;
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

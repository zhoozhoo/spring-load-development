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

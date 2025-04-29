package ca.zhoozhoo.loaddev.loads.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "groups")
public record Group(
        @Id Long id,

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
}

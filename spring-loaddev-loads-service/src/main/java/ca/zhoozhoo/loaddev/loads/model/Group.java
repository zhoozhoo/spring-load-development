package ca.zhoozhoo.loaddev.loads.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Table(name = "groups")
public record Group(
        @Id Long id,

        @NotBlank(message = "Owner ID is required") 
        @Column("owner_id") String ownerId,

        @NotNull(message = "Date is required")
        @Column("date") LocalDate date,

        @NotNull(message = "Powder charge is required")
        @Positive(message = "Powder charge must be positive")
        @Column("powder_charge") Double powderCharge,

        @NotNull(message = "Powder charge unit is required")
        @Column("powder_charge_unit") Unit powderChargeUnit,

        @NotNull(message = "Target range is required")
        @Positive(message = "Target range must be positive")
        @Column("target_range") Integer targetRange,

        @NotNull(message = "Target range unit is required")
        @Column("target_range_unit") Unit targetRangeUnit,

        @Positive(message = "Group size must be positive")
        @Column("group_size") Double groupSize,

        @Column("group_size_unit") Unit groupSizeUnit
) {}

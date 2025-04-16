package ca.zhoozhoo.loaddev.loads.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "groups")
public record Group(

        @Id Long id,

        @NotNull @Column String ownerId,

        @NotNull @Positive @Column Integer numberOfShots,

        @NotNull @Positive @Column Integer targetRange,

        @NotNull @Positive @Column Double groupSize,

        @NotNull @Column Integer mean,

        @NotNull @Column Integer median,

        @NotNull @Column Integer min,

        @NotNull @Column Integer max,

        @NotNull @Min(0) @Column Integer standardDeviation,

        @NotNull @Positive @Column Integer extremeSpread) {
}

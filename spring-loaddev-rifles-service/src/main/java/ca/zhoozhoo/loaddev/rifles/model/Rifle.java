package ca.zhoozhoo.loaddev.rifles.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "rifles")
public record Rifle(
        @Id Long id,

        @NotBlank(message = "Name is required")
        @Column("name") String name,

        @Column("description") String description,

        @NotBlank(message = "Caliber is required")
        @Column("caliber") String caliber,

        @NotNull(message = "Barrel length is required")
        @Positive(message = "Barrel length must be positive")
        @Column("barrel_length") Double barrelLength,

        @Column("barrel_contour") String barrelContour,

        @NotBlank(message = "Twist rate is required")
        @Column("twist_rate") String twistRate,

        @NotNull(message = "Free bore is required")
        @Positive(message = "Free bore must be positive")
        @Column("free_bore") Double freeBore,

        @Column("rifling") String rifling) {
}
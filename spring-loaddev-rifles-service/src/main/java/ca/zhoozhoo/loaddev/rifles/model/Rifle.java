package ca.zhoozhoo.loaddev.rifles.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Table(name = "rifles")
public record Rifle(

        @Id Long id,

        @Column("owner_id") String ownerId,

        @NotBlank(message = "Name is required")
        @Column("name") String name,

        @Column("description") String description,

        @NotBlank(message = "Measurement Units is required")
        @Column("measurement_units") String measurementUnits,

        @NotBlank(message = "Caliber is required")
        @Column("caliber") String caliber,

        @Positive(message = "Barrel length must be positive")
        @Column("barrel_length") Double barrelLength,

        @Column("barrel_contour") String barrelContour,

        @Column("twist_rate") String twistRate,

        @Column("rifling") String rifling,

        @Positive(message = "Free bore must be positive")
        @Column("free_bore") Double freeBore) {

    public static final String METRIC = "Metric";

    public static final String IMPERIAL = "Imperial";
}
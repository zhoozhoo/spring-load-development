package ca.zhoozhoo.loaddev.loads.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import ca.zhoozhoo.loaddev.loads.validation.LoadMeasurement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "loads")
@LoadMeasurement
public record Load(

        @Id Long id,

        @Column String ownerId,

        @NotBlank(message = "Name is required")
        @Column("name") String name,

        @Column("description") String description,

        @NotBlank(message = "Measurement Units is required")
        @Column("measurement_units") String measurementUnits,

        @NotBlank(message = "Powder manufacturer is required")
        @Column("powder_manufacturer") String powderManufacturer,

        @NotBlank(message = "Powder type is required")
        @Column("powder_type") String powderType,

        @NotBlank(message = "Bullet manufacturer is required")
        @Column("bullet_manufacturer") String bulletManufacturer,

        @NotBlank(message = "Bullet type is required")
        @Column("bullet_type") String bulletType,

        @NotNull(message = "Bullet weight is required")
        @Positive(message = "Bullet weight must be positive")
        @Column("bullet_weight") Double bulletWeight,

        @NotBlank(message = "Primer manufacturer is required")
        @Column("primer_manufacturer") String primerManufacturer,

        @NotBlank(message = "Primer type is required")
        @Column("primer_type") String primerType,

        @Positive(message = "Distance from lands must be positive")
        @Column("distance_from_lands") Double distanceFromLands,

        @Positive(message = "Case overall length must be positive")
        @Column("case_overall_length") Double caseOverallLength,

        @Positive(message = "Neck tension must be positive")
        @Column("neck_tension") Double neckTension,
        
        @Column("rifle_id") Long rifleId) {

    public static final String METRIC = "Metric";

    public static final String IMPERIAL = "Imperial";
}
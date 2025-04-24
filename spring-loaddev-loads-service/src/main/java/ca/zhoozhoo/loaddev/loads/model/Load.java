package ca.zhoozhoo.loaddev.loads.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ca.zhoozhoo.loaddev.loads.validation.LoadMeasurement;

@Table(name = "loads")
@LoadMeasurement
public record Load(

        @Id Long id,

        @NotBlank(message = "Owner ID is required")
        @Column String ownerId,

        @NotBlank(message = "Name is required")
        @Column("name") String name,

        @Column("description") String description,

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

        @NotNull(message = "Bullet weight unit is required")
        @Column("bullet_weight_unit") Unit bulletWeightUnit,

        @NotBlank(message = "Primer manufacturer is required")
        @Column("primer_manufacturer") String primerManufacturer,

        @NotBlank(message = "Primer type is required")
        @Column("primer_type") String primerType,

        @Positive(message = "Distance from lands must be positive")
        @Column("distance_from_lands") Double distanceFromLands,

        @Column("distance_from_lands_unit") Unit distanceFromLandsUnit,

        @Positive(message = "Case overall length must be positive")
        @Column("case_overall_length") Double caseOverallLength,

        @Column("case_overall_length_unit") Unit caseOverallLengthUnit,

        @Positive(message = "Neck tension must be positive")
        @Column("neck_tension") Double neckTension,

        @Column("neck_tension_unit") Unit neckTensionUnit,
        
        @Column("rifle_id") Long rifleId) {
}
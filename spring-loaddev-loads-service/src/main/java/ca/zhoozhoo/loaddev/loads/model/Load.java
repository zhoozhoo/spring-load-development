package ca.zhoozhoo.loaddev.loads.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.zhoozhoo.loaddev.loads.validation.LoadMeasurement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "loads")
@LoadMeasurement
public record Load(

        @Id Long id,

        @JsonIgnore
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Load load = (Load) o;
        return Objects.equals(id, load.id) &&
                Objects.equals(name, load.name) &&
                Objects.equals(description, load.description) &&
                Objects.equals(measurementUnits, load.measurementUnits) &&
                Objects.equals(powderManufacturer, load.powderManufacturer) &&
                Objects.equals(powderType, load.powderType) &&
                Objects.equals(bulletManufacturer, load.bulletManufacturer) &&
                Objects.equals(bulletType, load.bulletType) &&
                Objects.equals(bulletWeight, load.bulletWeight) &&
                Objects.equals(primerManufacturer, load.primerManufacturer) &&
                Objects.equals(primerType, load.primerType) &&
                Objects.equals(distanceFromLands, load.distanceFromLands) &&
                Objects.equals(caseOverallLength, load.caseOverallLength) &&
                Objects.equals(neckTension, load.neckTension) &&
                Objects.equals(rifleId, load.rifleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, measurementUnits, 
                powderManufacturer, powderType, bulletManufacturer, bulletType,
                bulletWeight, primerManufacturer, primerType, distanceFromLands,
                caseOverallLength, neckTension, rifleId);
    }
}
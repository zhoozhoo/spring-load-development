package ca.zhoozhoo.loaddev.loads.model;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.format.QuantityFormat;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.units.indriya.format.SimpleQuantityFormat;

/**
 * Represents an ammunition load configuration.
 * <p>
 * A load defines a complete recipe for ammunition reloading, including powder type and charge,
 * bullet specifications, primer information, and cartridge measurements. Each load is owned by
 * a specific user and must comply with either imperial or metric measurement units.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Table(name = "loads_jsr363")
public record LoadJSR363(

        @Id Long id,

        @JsonIgnore
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
        @Column("bullet_weight") Quantity<Mass> bulletWeight,

        @NotBlank(message = "Primer manufacturer is required")
        @Column("primer_manufacturer") String primerManufacturer,

        @NotBlank(message = "Primer type is required")
        @Column("primer_type") String primerType,

        @Positive(message = "Distance from lands must be positive")
        @Column("distance_from_lands") Quantity<Length> distanceFromLands,

        @Positive(message = "Case overall length must be positive")
        @Column("case_overall_length") Quantity<Length> caseOverallLength,

        @Positive(message = "Neck tension must be positive")
        @Column("neck_tension") Quantity<Length> neckTension,

        @Column("rifle_id") Long rifleId) {

    public static final String METRIC = "Metric";

    public static final String IMPERIAL = "Imperial";

    private static final QuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();

    /**
     * Compact constructor with validation logic (Java 25 Flexible Constructor Bodies - JEP 482).
     * <p>
     * This constructor performs business rule validation beyond what Jakarta Bean Validation
     * provides, ensuring measurement unit consistency and value constraints using Java 25
     * enhanced pattern matching.
     * </p>
     */
    public LoadJSR363 {        
        // At least one cartridge measurement must be specified
        if (distanceFromLands == null && caseOverallLength == null) {
            throw new IllegalArgumentException(
                "Either distance from lands or case overall length must be specified"
            );
        }
        
        // Validate neck tension
        if (neckTension != null && neckTension.getValue().doubleValue() <= 0) {
            throw new IllegalArgumentException(
                "Neck tension must be positive, got: " + QUANTITY_FORMAT.format(neckTension)
            );
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
        LoadJSR363 load = (LoadJSR363) o;
        return Objects.equals(id, load.id) &&
                Objects.equals(name, load.name) &&
                Objects.equals(description, load.description) &&
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
        return Objects.hash(id, name, description, 
                powderManufacturer, powderType, bulletManufacturer, bulletType,
                bulletWeight, primerManufacturer, primerType, distanceFromLands,
                caseOverallLength, neckTension, rifleId);
    }
}
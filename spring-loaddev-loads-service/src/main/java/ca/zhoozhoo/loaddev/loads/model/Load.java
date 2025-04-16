package ca.zhoozhoo.loaddev.loads.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;

@Table(name = "loads")
public record Load(

        @Id Long id,

        @NotNull @Column String ownerId,

        @NotNull @Column("name") String name,

        @Column("description") String description,

        @NotNull @Column("powder_manufacturer") String powderManufacturer,

        @NotNull @Column("powder_type") String powderType,

        @NotNull @Column("powder_charge") Double powderCharge,

        @NotNull @Column("bullet_manufacturer") String bulletManufacturer,

        @NotNull @Column("bullet_type") String bulletType,

        @NotNull @Column("bullet_weight") Double bulletWeight,

        @NotNull @Column("primer_manufacturer") String primerManufacturer,

        @NotNull @Column("primer_type") String primerType,

        @NotNull @Column("distance_from_lands") Double distanceFromLands,

        @NotNull @Column("rifle_id") Long rifleId) {
    public Load {
    }
}
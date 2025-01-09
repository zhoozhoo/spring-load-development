package ca.zhoozhoo.loaddev.load_development.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "loads")
public record Load(

        @Id Long id,

        @Column("name") String name,

        @Column("description") String description,

        @Column("powder_manufacturer") String powderManufacturer,

        @Column("powder_type") String powderType,

        @Column("powder_charge") Double powderCharge,

        @Column("bullet_manufacturer") String bulletManufacturer,

        @Column("bullet_type") String bulletType,

        @Column("bullet_weight") Double bulletWeight,

        @Column("primer_manufacturer") String primerManufacturer,

        @Column("primer_type") String primerType,

        @Column("distance_from_lands") Double distanceFromLands,

        @Column("rifle_id") Long rifleId) {
    public Load {
        if (name == null || powderManufacturer == null || powderType == null || powderCharge == null
                || bulletManufacturer == null || bulletType == null || bulletWeight == null
                || primerManufacturer == null || primerType == null || rifleId == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
    }
}
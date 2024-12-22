package ca.zhoozhoo.loaddev.load_development.model;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EntityScan
@Table(name = "rifles")
public record Rifle(

        @Id Long id,

        @Column("name") String name,

        @Column("description") String description,

        @Column("caliber") String caliber,

        @Column("barrel_length") Double barrelLength,

        @Column("barrel_contour") String barrelContour,

        @Column("twist_rate") String twistRate,

        @Column("free_bore") Double freeBore,

        @Column("rifling") String rifling) {
}
package ca.zhoozhoo.loaddev.load_development.model;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EntityScan
@Table(name = "loads")
public record Load(
        @Id Long id,

        @Column("name") String name,

        @Column("description") String description,

        @Column("weight") Double weight

) {
}

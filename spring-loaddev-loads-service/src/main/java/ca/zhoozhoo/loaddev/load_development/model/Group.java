package ca.zhoozhoo.loaddev.load_development.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "groups")
public record Group(

        @Id Long id,

        @Column Integer numberOfShots,

        @Column Integer targetRange,

        @Column Double groupSize,

        @Column Integer mean,

        @Column Integer median,

        @Column Integer min,

        @Column Integer max,

        @Column Integer standardDeviation,

        @Column Integer extremeSpread) {
}

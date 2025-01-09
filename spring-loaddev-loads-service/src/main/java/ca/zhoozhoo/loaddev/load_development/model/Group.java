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

        @Column Double mean,

        @Column Double median,

        @Column Double min,

        @Column Double max,

        @Column Double standardDeviation,

        @Column Double extremeSpread) {
}

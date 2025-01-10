package ca.zhoozhoo.loaddev.load_development.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "shots")
public record Shot(

        @Id Long id,

        @Column Long groupId,
        
        @Column Integer velocity) {
}

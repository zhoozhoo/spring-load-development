package ca.zhoozhoo.loaddev.load_development.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "shots")
public record Shot(

        @Id Long id,

        @NotNull
        @Column Long groupId,
        
        @Positive
        @Column Integer velocity) {
}

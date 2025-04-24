package ca.zhoozhoo.loaddev.loads.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Table(name = "shots")
public record Shot(

        @Id Long id,

        @NotBlank(message = "Owner ID is required")
        @Column("owner_id") String ownerId,

        @NotNull(message = "Group ID is required")
        @Column("group_id") Long groupId,

        @Column("velocity") Integer velocity,

        @Column("velocity_unit") Unit velocityUnit) {
}

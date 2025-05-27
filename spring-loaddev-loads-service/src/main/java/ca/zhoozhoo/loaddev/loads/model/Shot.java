package ca.zhoozhoo.loaddev.loads.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table(name = "shots")
public record Shot(

        @Id Long id,

        @JsonIgnore
        @Column("owner_id") String ownerId,

        @NotNull(message = "Group ID is required")
        @Column("group_id") Long groupId,

        @Positive(message = "Velocity must be a positive number")
        @Column("velocity") Integer velocity) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shot shot = (Shot) o;
        return Objects.equals(id, shot.id) &&
                Objects.equals(groupId, shot.groupId) &&
                Objects.equals(velocity, shot.velocity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, velocity);
    }
}

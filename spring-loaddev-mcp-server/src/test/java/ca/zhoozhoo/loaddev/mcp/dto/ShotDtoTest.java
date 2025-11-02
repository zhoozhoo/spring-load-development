package ca.zhoozhoo.loaddev.mcp.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ShotDto record.
 * <p>
 * Tests the data transfer object representing a single shot measurement.
 * 
 * @author Zhubin Salehi
 */
class ShotDtoTest {

    /**
     * Tests creating a ShotDto with a velocity value.
     */
    @Test
    void constructor_WithVelocity_ShouldCreateInstance() {
        // Given
        Integer velocity = 2850;

        // When
        ShotDto shotDto = new ShotDto(velocity);

        // Then
        assertThat(shotDto.velocity()).isEqualTo(velocity);
    }

    /**
     * Tests creating a ShotDto with null velocity.
     */
    @Test
    void constructor_WithNullVelocity_ShouldCreateInstance() {
        // When
        ShotDto shotDto = new ShotDto(null);

        // Then
        assertThat(shotDto.velocity()).isNull();
    }

    /**
     * Tests creating a ShotDto with zero velocity.
     */
    @Test
    void constructor_WithZeroVelocity_ShouldCreateInstance() {
        // When
        ShotDto shotDto = new ShotDto(0);

        // Then
        assertThat(shotDto.velocity()).isEqualTo(0);
    }

    /**
     * Tests record equality based on velocity.
     */
    @Test
    void equals_WithSameVelocity_ShouldBeEqual() {
        // Given
        ShotDto shotDto1 = new ShotDto(2850);
        ShotDto shotDto2 = new ShotDto(2850);

        // Then
        assertThat(shotDto1).isEqualTo(shotDto2);
        assertThat(shotDto1.hashCode()).isEqualTo(shotDto2.hashCode());
    }

    /**
     * Tests record inequality when velocities differ.
     */
    @Test
    void equals_WithDifferentVelocities_ShouldNotBeEqual() {
        // Given
        ShotDto shotDto1 = new ShotDto(2850);
        ShotDto shotDto2 = new ShotDto(2860);

        // Then
        assertThat(shotDto1).isNotEqualTo(shotDto2);
    }

    /**
     * Tests toString includes velocity value.
     */
    @Test
    void toString_ShouldIncludeVelocity() {
        // Given
        ShotDto shotDto = new ShotDto(2850);

        // When
        String result = shotDto.toString();

        // Then
        assertThat(result)
                .contains("ShotDto")
                .contains("2850");
    }

    /**
     * Tests multiple shots with different velocities.
     */
    @Test
    void multipleShots_WithDifferentVelocities_ShouldBeDistinct() {
        // Given
        ShotDto shot1 = new ShotDto(2845);
        ShotDto shot2 = new ShotDto(2850);
        ShotDto shot3 = new ShotDto(2855);

        // Then
        assertThat(shot1).isNotEqualTo(shot2);
        assertThat(shot2).isNotEqualTo(shot3);
        assertThat(shot1).isNotEqualTo(shot3);
        assertThat(shot1.velocity()).isLessThan(shot2.velocity());
        assertThat(shot2.velocity()).isLessThan(shot3.velocity());
    }
}

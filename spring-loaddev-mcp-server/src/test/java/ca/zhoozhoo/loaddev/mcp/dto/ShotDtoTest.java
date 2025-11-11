package ca.zhoozhoo.loaddev.mcp.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.Test;

class ShotDtoTest {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Quantity<Speed> speed(double v) {
        return (Quantity) getQuantity(v, METRE.divide(SECOND));
    }

    @Test
    void constructor_WithVelocity_ShouldCreateInstance() {
        Quantity<Speed> velocity = speed(2850.0);
        ShotDto shotDto = new ShotDto(velocity);
        assertThat(shotDto.velocity()).isEqualTo(velocity);
    }

    @Test
    void constructor_WithNullVelocity_ShouldCreateInstance() {
        ShotDto shotDto = new ShotDto(null);
        assertThat(shotDto.velocity()).isNull();
    }

    @Test
    void constructor_WithZeroVelocity_ShouldCreateInstance() {
        ShotDto shotDto = new ShotDto(speed(0.0));
        assertThat(shotDto.velocity().getValue().doubleValue()).isEqualTo(0.0);
    }

    @Test
    void equals_WithSameVelocity_ShouldBeEqual() {
        ShotDto a = new ShotDto(speed(2850.0));
        ShotDto b = new ShotDto(speed(2850.0));
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_WithDifferentVelocities_ShouldNotBeEqual() {
        ShotDto a = new ShotDto(speed(2850.0));
        ShotDto b = new ShotDto(speed(2860.0));
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toString_ShouldIncludeVelocity() {
        ShotDto shotDto = new ShotDto(speed(2850.0));
        String result = shotDto.toString();
        assertThat(result).contains("ShotDto").contains("2850 m/s");
    }

    @Test
    void multipleShots_WithDifferentVelocities_ShouldBeDistinct() {
        ShotDto shot1 = new ShotDto(speed(2845.0));
        ShotDto shot2 = new ShotDto(speed(2850.0));
        ShotDto shot3 = new ShotDto(speed(2855.0));
        assertThat(shot1).isNotEqualTo(shot2);
        assertThat(shot2).isNotEqualTo(shot3);
        assertThat(shot1).isNotEqualTo(shot3);
        assertThat(shot1.velocity().getValue().doubleValue()).isLessThan(shot2.velocity().getValue().doubleValue());
        assertThat(shot2.velocity().getValue().doubleValue()).isLessThan(shot3.velocity().getValue().doubleValue());
    }
}

package ca.zhoozhoo.loaddev.mcp.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.Test;

class GroupDtoTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Quantity<Speed> speed(double v) { return (Quantity) getQuantity(v, METRE.divide(SECOND)); }

    @Test
    void constructor_full() {
        var dto = new GroupDto(
                LocalDate.of(2025, 11, 11),
                getQuantity(42.5, GRAM),
                getQuantity(100.0, METRE),
                getQuantity(0.75, METRE),
                speed(2850.5),
                speed(15.2),
                speed(45.0),
                List.of(new ShotDto(speed(2845)), new ShotDto(speed(2855))));
        assertThat(dto.shots()).hasSize(2);
        assertThat(dto.powderCharge().getValue().doubleValue()).isEqualTo(42.5);
    }

    @Test
    void defensiveCopy_shots() {
        var shots = new ArrayList<ShotDto>();
        shots.add(new ShotDto(speed(2845)));
        var dto = new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), speed(2850.5), speed(15.2), speed(45.0), shots);
        shots.add(new ShotDto(speed(2860)));
        assertThat(dto.shots()).hasSize(1);
    }

    @Test
    void nullShots_becomesEmpty() {
        var dto = new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), speed(2850.5), speed(15.2), speed(45.0), null);
        assertThat(dto.shots()).isEmpty();
    }

    @Test
    void equality() {
        var shots = List.of(new ShotDto(speed(2845)));
        var a = new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), speed(2850.5), speed(15.2), speed(45.0), shots);
        var b = new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), speed(2850.5), speed(15.2), speed(45.0), shots);
        assertThat(a).isEqualTo(b);
        var c = new GroupDto(LocalDate.now(), getQuantity(43.0, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), speed(2850.5), speed(15.2), speed(45.0), shots);
        assertThat(a).isNotEqualTo(c);
    }
}

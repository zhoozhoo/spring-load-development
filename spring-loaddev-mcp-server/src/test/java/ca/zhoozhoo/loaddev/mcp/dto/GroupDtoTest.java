package ca.zhoozhoo.loaddev.mcp.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for GroupDto record.
 * <p>
 * Tests the data transfer object representing a shooting group with ballistic statistics,
 * including validation of defensive copying of mutable collections.
 * 
 * @author Zhubin Salehi
 */
class GroupDtoTest {

    /**
     * Tests creating a GroupDto with all fields populated.
     */
    @Test
    void constructor_WithAllFields_ShouldCreateInstance() {
        // Given
        LocalDate date = LocalDate.of(2024, 10, 15);
        Double powderCharge = 42.5;
        Integer targetRange = 100;
        Double groupSize = 0.75;
        double averageVelocity = 2850.5;
        double standardDeviation = 15.2;
        double extremeSpread = 45.0;
        List<ShotDto> shots = List.of(
                new ShotDto(2845),
                new ShotDto(2855),
                new ShotDto(2850)
        );

        // When
        GroupDto groupDto = new GroupDto(
                date,
                powderCharge,
                targetRange,
                groupSize,
                averageVelocity,
                standardDeviation,
                extremeSpread,
                shots
        );

        // Then
        assertThat(groupDto.date()).isEqualTo(date);
        assertThat(groupDto.powderCharge()).isEqualTo(powderCharge);
        assertThat(groupDto.targetRange()).isEqualTo(targetRange);
        assertThat(groupDto.groupSize()).isEqualTo(groupSize);
        assertThat(groupDto.averageVelocity()).isEqualTo(averageVelocity);
        assertThat(groupDto.standardDeviation()).isEqualTo(standardDeviation);
        assertThat(groupDto.extremeSpread()).isEqualTo(extremeSpread);
        assertThat(groupDto.shots()).hasSize(3);
        assertThat(groupDto.shots()).containsExactlyElementsOf(shots);
    }

    /**
     * Tests that the compact constructor creates a defensive copy of the shots list.
     */
    @Test
    void constructor_WithMutableList_ShouldCreateDefensiveCopy() {
        // Given
        List<ShotDto> originalShots = new ArrayList<>();
        originalShots.add(new ShotDto(2845));
        originalShots.add(new ShotDto(2855));

        // When
        GroupDto groupDto = new GroupDto(
                LocalDate.now(),
                42.5,
                100,
                0.75,
                2850.5,
                15.2,
                45.0,
                originalShots
        );

        // Then
        assertThat(groupDto.shots()).hasSize(2);
        
        // Modify original list
        originalShots.add(new ShotDto(2860));
        
        // GroupDto should still have 2 shots (defensive copy)
        assertThat(groupDto.shots()).hasSize(2);
    }

    /**
     * Tests that null shots list is converted to empty list.
     */
    @Test
    void constructor_WithNullShots_ShouldUseEmptyList() {
        // When
        GroupDto groupDto = new GroupDto(
                LocalDate.now(),
                42.5,
                100,
                0.75,
                2850.5,
                15.2,
                45.0,
                null
        );

        // Then
        assertThat(groupDto.shots()).isNotNull();
        assertThat(groupDto.shots()).isEmpty();
    }

    /**
     * Tests that the shots list is immutable.
     */
    @Test
    void shots_ShouldBeImmutable() {
        // Given
        GroupDto groupDto = new GroupDto(
                LocalDate.now(),
                42.5,
                100,
                0.75,
                2850.5,
                15.2,
                45.0,
                List.of(new ShotDto(2845))
        );

        // When/Then
        assertThat(groupDto.shots()).hasSize(1);
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
                groupDto.shots().add(new ShotDto(2855)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests creating a GroupDto with null optional fields.
     */
    @Test
    void constructor_WithNullOptionalFields_ShouldCreateInstance() {
        // When
        GroupDto groupDto = new GroupDto(
                null,  // date
                null,  // powderCharge
                null,  // targetRange
                null,  // groupSize
                2850.5,
                15.2,
                45.0,
                List.of()
        );

        // Then
        assertThat(groupDto.date()).isNull();
        assertThat(groupDto.powderCharge()).isNull();
        assertThat(groupDto.targetRange()).isNull();
        assertThat(groupDto.groupSize()).isNull();
        assertThat(groupDto.averageVelocity()).isEqualTo(2850.5);
        assertThat(groupDto.standardDeviation()).isEqualTo(15.2);
        assertThat(groupDto.extremeSpread()).isEqualTo(45.0);
    }

    /**
     * Tests record equality based on all fields.
     */
    @Test
    void equals_WithSameValues_ShouldBeEqual() {
        // Given
        LocalDate date = LocalDate.of(2024, 10, 15);
        List<ShotDto> shots = List.of(new ShotDto(2845));
        
        GroupDto groupDto1 = new GroupDto(date, 42.5, 100, 0.75, 2850.5, 15.2, 45.0, shots);
        GroupDto groupDto2 = new GroupDto(date, 42.5, 100, 0.75, 2850.5, 15.2, 45.0, shots);

        // Then
        assertThat(groupDto1).isEqualTo(groupDto2);
        assertThat(groupDto1.hashCode()).isEqualTo(groupDto2.hashCode());
    }

    /**
     * Tests record inequality when values differ.
     */
    @Test
    void equals_WithDifferentValues_ShouldNotBeEqual() {
        // Given
        LocalDate date = LocalDate.of(2024, 10, 15);
        List<ShotDto> shots = List.of(new ShotDto(2845));
        
        GroupDto groupDto1 = new GroupDto(date, 42.5, 100, 0.75, 2850.5, 15.2, 45.0, shots);
        GroupDto groupDto2 = new GroupDto(date, 43.0, 100, 0.75, 2850.5, 15.2, 45.0, shots);

        // Then
        assertThat(groupDto1).isNotEqualTo(groupDto2);
    }

    /**
     * Tests toString includes all field values.
     */
    @Test
    void toString_ShouldIncludeAllFields() {
        // Given
        LocalDate date = LocalDate.of(2024, 10, 15);
        GroupDto groupDto = new GroupDto(date, 42.5, 100, 0.75, 2850.5, 15.2, 45.0, List.of());

        // When
        String result = groupDto.toString();

        // Then
        assertThat(result).contains("GroupDto");
        assertThat(result).contains("2024-10-15");
        assertThat(result).contains("42.5");
        assertThat(result).contains("100");
        assertThat(result).contains("0.75");
        assertThat(result).contains("2850.5");
        assertThat(result).contains("15.2");
        assertThat(result).contains("45.0");
    }
}

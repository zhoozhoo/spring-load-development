package ca.zhoozhoo.loaddev.mcp.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.METRE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for LoadDetails record.
 * <p>
 * Tests the aggregated data structure combining load, rifle, and shooting group information,
 * including validation of defensive copying of mutable collections.
 * 
 * @author Zhubin Salehi
 */
class LoadDetailsTest {

    /**
     * Tests creating a LoadDetails with all fields populated.
     */
    @Test
    void constructor_WithAllFields_ShouldCreateInstance() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));
        List<GroupDto> groups = List.of(
                new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), null, null, null, List.of()),
                new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(200.0, METRE), getQuantity(1.25, METRE), null, null, null, List.of())
        );

        // When
        LoadDetails loadDetails = new LoadDetails(load, rifle, groups);

        // Then
        assertThat(loadDetails.load()).isEqualTo(load);
        assertThat(loadDetails.rifle()).isEqualTo(rifle);
        assertThat(loadDetails.groups()).hasSize(2);
        assertThat(loadDetails.groups()).containsExactlyElementsOf(groups);
    }

    /**
     * Tests that the compact constructor creates a defensive copy of the groups list.
     */
    @Test
    void constructor_WithMutableList_ShouldCreateDefensiveCopy() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));
        
        List<GroupDto> originalGroups = new ArrayList<>();
        originalGroups.add(new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), null, null, null, List.of()));

        // When
        LoadDetails loadDetails = new LoadDetails(load, rifle, originalGroups);

        // Then
        assertThat(loadDetails.groups()).hasSize(1);
        
        // Modify original list
        originalGroups.add(new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(200.0, METRE), getQuantity(1.25, METRE), null, null, null, List.of()));
        
        // LoadDetails should still have 1 group (defensive copy)
        assertThat(loadDetails.groups()).hasSize(1);
    }

    /**
     * Tests that null groups list is converted to empty list.
     */
    @Test
    void constructor_WithNullGroups_ShouldUseEmptyList() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));

        // When
        LoadDetails loadDetails = new LoadDetails(load, rifle, null);

        // Then
        assertThat(loadDetails.groups()).isNotNull();
        assertThat(loadDetails.groups()).isEmpty();
    }

    /**
     * Tests that the groups list is immutable.
     */
    @Test
    void groups_ShouldBeImmutable() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));
        LoadDetails loadDetails = new LoadDetails(load, rifle,
                List.of(new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), null, null, null, List.of())));

        // When/Then
        assertThat(loadDetails.groups()).hasSize(1);
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
                loadDetails.groups().add(new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(200.0, METRE), getQuantity(1.25, METRE), null, null, null, List.of())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests creating a LoadDetails with null load and rifle.
     */
    @Test
    void constructor_WithNullLoadAndRifle_ShouldCreateInstance() {
        // When
        LoadDetails loadDetails = new LoadDetails(null, null, List.of());

        // Then
        assertThat(loadDetails.load()).isNull();
        assertThat(loadDetails.rifle()).isNull();
        assertThat(loadDetails.groups()).isEmpty();
    }

    /**
     * Tests creating a LoadDetails with empty groups list.
     */
    @Test
    void constructor_WithEmptyGroups_ShouldCreateInstance() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));

        // When
        LoadDetails loadDetails = new LoadDetails(load, rifle, List.of());

        // Then
        assertThat(loadDetails.load()).isEqualTo(load);
        assertThat(loadDetails.rifle()).isEqualTo(rifle);
        assertThat(loadDetails.groups()).isEmpty();
    }

    /**
     * Tests record equality based on all fields.
     */
    @Test
    void equals_WithSameValues_ShouldBeEqual() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));
        List<GroupDto> groups = List.of(
                new GroupDto(LocalDate.now(), getQuantity(42.5, GRAM), getQuantity(100.0, METRE), getQuantity(0.75, METRE), null, null, null, List.of())
        );
        
        LoadDetails loadDetails1 = new LoadDetails(load, rifle, groups);
        LoadDetails loadDetails2 = new LoadDetails(load, rifle, groups);

        // Then
        assertThat(loadDetails1).isEqualTo(loadDetails2);
        assertThat(loadDetails1.hashCode()).isEqualTo(loadDetails2.hashCode());
    }

    /**
     * Tests record inequality when values differ.
     */
    @Test
    void equals_WithDifferentValues_ShouldNotBeEqual() {
        // Given
        LoadDto load1 = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        LoadDto load2 = new LoadDto(2L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));
        List<GroupDto> groups = List.of();
        
        LoadDetails loadDetails1 = new LoadDetails(load1, rifle, groups);
        LoadDetails loadDetails2 = new LoadDetails(load2, rifle, groups);

        // Then
        assertThat(loadDetails1).isNotEqualTo(loadDetails2);
    }

    /**
     * Tests toString includes all field values.
     */
    @Test
    void toString_ShouldIncludeAllFields() {
        // Given
        LoadDto load = new LoadDto(1L, "308 Win Load", "Test load",
                "Hodgdon", "Varget", "Hornady", "BTHP", getQuantity(168.0, GRAM),
                "CCI", "BR-2", getQuantity(0.020, METRE), getQuantity(2.800, METRE), getQuantity(0.002, METRE), 1L);
        RifleDto rifle = new RifleDto(1L, "Remington 700", "Precision rifle",
                "308 Winchester", getQuantity(24.0, METRE), "Heavy", "1:10", "Button", getQuantity(0.0, METRE));
        LoadDetails loadDetails = new LoadDetails(load, rifle, List.of());

        // When
        String result = loadDetails.toString();

        // Then
        assertThat(result).contains("LoadDetails");
        assertThat(result).contains("load=");
        assertThat(result).contains("rifle=");
        assertThat(result).contains("groups=");
    }
}

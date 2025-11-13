package ca.zhoozhoo.loaddev.rifles.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for Rifle model validation.
 * Tests JSR-385 quantity validation and edge cases.
 *
 * @author Zhubin Salehi
 */
class RifleTest {

    @Test
    void createRifle_withValidBarrelLength_shouldSucceed() {
        assertEquals(26.0, new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", "1:8", "5R", getQuantity(0.157, INCH_INTERNATIONAL))
                .barrelLength().getValue().doubleValue(), 0.001);
    }

    @Test
    void createRifle_withBarrelLengthAtMinimum_shouldSucceed() {
        assertEquals(4.0, new Rifle(1L, "owner123", "Test Rifle", "Description",
                ".308", getQuantity(4.0, INCH_INTERNATIONAL),
                "Contour", "1:10", "4 Groove", getQuantity(0.001, INCH_INTERNATIONAL))
                .barrelLength().getValue().doubleValue(), 0.001);
    }

    @Test
    void createRifle_withBarrelLengthAtMaximum_shouldSucceed() {
        assertEquals(50.0, new Rifle(1L, "owner123", "Test Rifle", "Description",
                ".338 Lapua", getQuantity(50.0, INCH_INTERNATIONAL),
                "Heavy", "1:10", "6 Groove", getQuantity(0.25, INCH_INTERNATIONAL))
                .barrelLength().getValue().doubleValue(), 0.001);
    }

    @Test
    void createRifle_withBarrelLengthBelowMinimum_shouldThrow() {
        assertEquals("Barrel length must be between 4.0 and 50.0 inches, got: 3.99",
                assertThrows(IllegalArgumentException.class,
                        () -> new Rifle(1L, "owner123", "Test Rifle", "Description",
                                ".22 LR", getQuantity(3.99, INCH_INTERNATIONAL),
                                "Contour", "1:16", "Rifling", getQuantity(0.01, INCH_INTERNATIONAL)))
                        .getMessage());
    }

    @Test
    void createRifle_withBarrelLengthAboveMaximum_shouldThrow() {
        assertEquals("Barrel length must be between 4.0 and 50.0 inches, got: 50.01",
                assertThrows(IllegalArgumentException.class,
                        () -> new Rifle(1L, "owner123", "Test Rifle", "Description",
                                ".50 BMG", getQuantity(50.01, INCH_INTERNATIONAL),
                                "Heavy", "1:15", "Rifling", getQuantity(0.2, INCH_INTERNATIONAL)))
                        .getMessage());
    }

    @Test
    void createRifle_withFreeBoreAtMinimum_shouldSucceed() {
        assertEquals(0.001, new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", "1:8", "5R", getQuantity(0.001, INCH_INTERNATIONAL))
                .freeBore().getValue().doubleValue(), 0.0001);
    }

    @Test
    void createRifle_withFreeBoreAtMaximum_shouldSucceed() {
        assertEquals(0.5, new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", "1:8", "5R", getQuantity(0.5, INCH_INTERNATIONAL))
                .freeBore().getValue().doubleValue(), 0.0001);
    }

    @Test
    void createRifle_withFreeBoreBelowMinimum_shouldThrow() {
        assertEquals("Free bore must be between 0.001 and 0.5 inches, got: 0.0009",
                assertThrows(IllegalArgumentException.class,
                        () -> new Rifle(1L, "owner123", "Test Rifle", "Description",
                                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                                "Heavy Palma", "1:8", "5R", getQuantity(0.0009, INCH_INTERNATIONAL)))
                        .getMessage());
    }

    @Test
    void createRifle_withFreeBoreAboveMaximum_shouldThrow() {
        assertEquals("Free bore must be between 0.001 and 0.5 inches, got: 0.5010",
                assertThrows(IllegalArgumentException.class,
                        () -> new Rifle(1L, "owner123", "Test Rifle", "Description",
                                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                                "Heavy Palma", "1:8", "5R", getQuantity(0.501, INCH_INTERNATIONAL)))
                        .getMessage());
    }

    @Test
    void createRifle_withNullBarrelLength_shouldSucceed() {
        new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", null,
                "Heavy Palma", "1:8", "5R", getQuantity(0.157, INCH_INTERNATIONAL));
    }

    @Test
    void createRifle_withNullFreeBore_shouldSucceed() {
        new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", "1:8", "5R", null);
    }

    @Test
    void createRifle_withBothQuantitiesNull_shouldSucceed() {
        new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", null, "Heavy Palma", "1:8", "5R", null);
    }
}

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

    /**
     * Helper method to create a Rifling object for testing.
     */
    private static Rifling rifling(double twistRateInches, TwistDirection direction) {
        return new Rifling(getQuantity(twistRateInches, INCH_INTERNATIONAL), direction, 6);
    }

    /**
     * Helper method to create a Rifling object with default RIGHT direction.
     */
    private static Rifling rifling(double twistRateInches) {
        return rifling(twistRateInches, TwistDirection.RIGHT);
    }

    @Test
    void createRifle_withValidBarrelLength_shouldSucceed() {
        assertEquals(26.0, new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null)
                .barrelLength().getValue().doubleValue(), 0.001);
    }

    @Test
    void createRifle_withBarrelLengthAtMinimum_shouldSucceed() {
        assertEquals(4.0, new Rifle(1L, "owner123", "Test Rifle", "Description",
                ".308", getQuantity(4.0, INCH_INTERNATIONAL),
                "Contour", rifling(10.0), null)
                .barrelLength().getValue().doubleValue(), 0.001);
    }

    @Test
    void createRifle_withBarrelLengthAtMaximum_shouldSucceed() {
        assertEquals(50.0, new Rifle(1L, "owner123", "Test Rifle", "Description",
                ".338 Lapua", getQuantity(50.0, INCH_INTERNATIONAL),
                "Heavy", rifling(10.0), null)
                .barrelLength().getValue().doubleValue(), 0.001);
    }

    @Test
    void createRifle_withBarrelLengthBelowMinimum_shouldThrow() {
        assertEquals("Barrel length must be between 4.0 and 50.0 inches, got: 3.99",
                assertThrows(IllegalArgumentException.class,
                        () -> new Rifle(1L, "owner123", "Test Rifle", "Description",
                                ".22 LR", getQuantity(3.99, INCH_INTERNATIONAL),
                                "Contour", rifling(16.0), null))
                        .getMessage());
    }

    @Test
    void createRifle_withBarrelLengthAboveMaximum_shouldThrow() {
        assertEquals("Barrel length must be between 4.0 and 50.0 inches, got: 50.01",
                assertThrows(IllegalArgumentException.class,
                        () -> new Rifle(1L, "owner123", "Test Rifle", "Description",
                                ".50 BMG", getQuantity(50.01, INCH_INTERNATIONAL),
                                "Heavy", rifling(15.0), null))
                        .getMessage());
    }

    @Test
    void createRifle_withNullBarrelLength_shouldSucceed() {
        new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", null,
                "Heavy Palma", rifling(8.0), null);
    }

    @Test
    void createRifle_withNullRifling_shouldSucceed() {
        new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", null, null);
    }

    @Test
    void equals_withSameValues_shouldReturnTrue() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        assertEquals(rifle1, rifle2);
    }

    @Test
    void equals_withSameQuantityValueAndUnit_shouldReturnTrue() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        assertEquals(rifle1, rifle2);
    }

    @Test
    void equals_withDifferentQuantityValue_shouldReturnFalse() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(24.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        assertEquals(false, rifle1.equals(rifle2));
    }

    @Test
    void equals_withDifferentRiflingTwistRate_shouldReturnFalse() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(10.0), null);
        assertEquals(false, rifle1.equals(rifle2));
    }

    @Test
    void equals_withDifferentRiflingDirection_shouldReturnFalse() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0, TwistDirection.RIGHT), null);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0, TwistDirection.LEFT), null);
        assertEquals(false, rifle1.equals(rifle2));
    }

    @Test
    void hashCode_withSameValues_shouldReturnSameHashCode() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        assertEquals(rifle1.hashCode(), rifle2.hashCode());
    }

    @Test
    void hashCode_withDifferentValues_shouldReturnDifferentHashCode() {
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        Rifle rifle2 = new Rifle(2L, "owner456", "Different Rifle", "Different",
                ".308", getQuantity(24.0, INCH_INTERNATIONAL),
                "Medium", rifling(10.0), null);
        assertEquals(false, rifle1.hashCode() == rifle2.hashCode());
    }

    @Test
    void createRifle_withNullZeroing_shouldSucceed() {
        Rifle rifle = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), null);
        assertEquals(null, rifle.zeroing());
    }

    @Test
    void createRifle_withValidZeroing_shouldSucceed() {
        Zeroing zeroing = new Zeroing(
                getQuantity(1.5, INCH_INTERNATIONAL),
                getQuantity(100.0, INCH_INTERNATIONAL));
        Rifle rifle = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing);
        assertEquals(zeroing, rifle.zeroing());
    }

    @Test
    void equals_withSameZeroing_shouldReturnTrue() {
        Zeroing zeroing = new Zeroing(
                getQuantity(1.5, INCH_INTERNATIONAL),
                getQuantity(100.0, INCH_INTERNATIONAL));
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing);
        assertEquals(rifle1, rifle2);
    }

    @Test
    void equals_withDifferentZeroing_shouldReturnFalse() {
        Zeroing zeroing1 = new Zeroing(
                getQuantity(1.5, INCH_INTERNATIONAL),
                getQuantity(100.0, INCH_INTERNATIONAL));
        Zeroing zeroing2 = new Zeroing(
                getQuantity(1.5, INCH_INTERNATIONAL),
                getQuantity(200.0, INCH_INTERNATIONAL));
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing1);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing2);
        assertEquals(false, rifle1.equals(rifle2));
    }

    @Test
    void hashCode_withSameZeroing_shouldReturnSameHashCode() {
        Zeroing zeroing = new Zeroing(
                getQuantity(1.5, INCH_INTERNATIONAL),
                getQuantity(100.0, INCH_INTERNATIONAL));
        Rifle rifle1 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing);
        Rifle rifle2 = new Rifle(1L, "owner123", "Test Rifle", "Description",
                "6.5 Creedmoor", getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma", rifling(8.0), zeroing);
        assertEquals(rifle1.hashCode(), rifle2.hashCode());
    }
}

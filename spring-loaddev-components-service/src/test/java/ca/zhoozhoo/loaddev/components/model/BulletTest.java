package ca.zhoozhoo.loaddev.components.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ca.zhoozhoo.loaddev.components.model.Bullet.IMPERIAL;
import static ca.zhoozhoo.loaddev.components.model.Bullet.METRIC;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Bullet} model class.
 * Tests record construction, equality, hashCode, and business logic.
 *
 * @author Zhubin Salehi
 */
class BulletTest {

    @Test
    void shouldCreateBulletWithAllFields() {
        // given & when
        var bullet = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP", 
                               METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet.id()).isEqualTo(1L);
        assertThat(bullet.ownerId()).isEqualTo("user123");
        assertThat(bullet.manufacturer()).isEqualTo("Hornady");
        assertThat(bullet.weight()).isEqualTo(168.0);
        assertThat(bullet.type()).isEqualTo("BTHP");
        assertThat(bullet.measurementUnits()).isEqualTo(METRIC);
        assertThat(bullet.cost()).isEqualByComparingTo(new BigDecimal("45.99"));
        assertThat(bullet.currency()).isEqualTo("USD");
        assertThat(bullet.quantityPerBox()).isEqualTo(100);
    }

    @Test
    void shouldCreateBulletWithImperialUnits() {
        // given & when
        var bullet = new Bullet(1L, "user123", "Sierra", 175.0, "MatchKing",
                               IMPERIAL, new BigDecimal("52.50"), "CAD", 100);

        // then
        assertThat(bullet.measurementUnits()).isEqualTo(IMPERIAL);
    }

    @Test
    void shouldCreateBulletWithNullId() {
        // given & when
        var bullet = new Bullet(null, "user123", "Nosler", 150.0, "Ballistic Tip",
                               METRIC, new BigDecimal("40.00"), "USD", 50);

        // then
        assertThat(bullet.id()).isNull();
    }

    @Test
    void equalsAndHashCode_shouldBeEqualForSameBusinessData() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet1).isEqualTo(bullet2);
        assertThat(bullet1.hashCode()).isEqualTo(bullet2.hashCode());
    }

    @Test
    void equalsAndHashCode_shouldBeEqualWithDifferentOwnerId() {
        // given - same business data but different ownerId
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user999", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);

        // then - should be equal because ownerId is excluded from equals
        assertThat(bullet1).isEqualTo(bullet2);
        assertThat(bullet1.hashCode()).isEqualTo(bullet2.hashCode());
    }

    @Test
    void equals_shouldHandleStandardContracts() {
        // given
        var bullet = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                               METRIC, new BigDecimal("45.99"), "USD", 100);

        // then - reflexive, null safety, and type safety
        assertThat(bullet).isEqualTo(bullet);
        assertThat(bullet).isNotEqualTo(null);
        assertThat(bullet).isNotEqualTo("Not a bullet");
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(2L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentManufacturer() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Sierra", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentWeight() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 175.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentType() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 168.0, "ELD-M",
                                METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentMeasurementUnits() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                IMPERIAL, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCost() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("50.00"), "USD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCurrency() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "CAD", 100);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentQuantityPerBox() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 50);

        // then
        assertThat(bullet1).isNotEqualTo(bullet2);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentBusinessData() {
        // given
        var bullet1 = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                                METRIC, new BigDecimal("45.99"), "USD", 100);
        var bullet2 = new Bullet(2L, "user123", "Sierra", 175.0, "MatchKing",
                                IMPERIAL, new BigDecimal("52.50"), "CAD", 50);

        // then
        assertThat(bullet1.hashCode()).isNotEqualTo(bullet2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        // given & when
        var bullet = new Bullet(1L, "user123", "Hornady", 168.0, "BTHP",
                               METRIC, new BigDecimal("45.99"), "USD", 100);

        // then
        assertThat(bullet.toString()).contains("1", "user123", "Hornady", "168.0", "BTHP", "Metric", "45.99", "USD", "100");
    }

    @Test
    void shouldHaveCorrectMetricConstant() {
        assertThat(METRIC).isEqualTo("Metric");
    }

    @Test
    void shouldHaveCorrectImperialConstant() {
        assertThat(IMPERIAL).isEqualTo("Imperial");
    }
}

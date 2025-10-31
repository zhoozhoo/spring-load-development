package ca.zhoozhoo.loaddev.components.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE_MAGNUM;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_RIFLE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Primer} model class.
 * Tests record construction, equality, hashCode, and business logic.
 *
 * @author Zhubin Salehi
 */
class PrimerTest {

    @Test
    void shouldCreatePrimerWithAllFields() {
        // given & when
        var primer = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                               new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer.id()).isEqualTo(1L);
        assertThat(primer.ownerId()).isEqualTo("user123");
        assertThat(primer.manufacturer()).isEqualTo("CCI");
        assertThat(primer.type()).isEqualTo("BR2");
        assertThat(primer.primerSize()).isEqualTo(LARGE_RIFLE);
        assertThat(primer.cost()).isEqualByComparingTo(new BigDecimal("65.99"));
        assertThat(primer.currency()).isEqualTo("USD");
        assertThat(primer.quantityPerBox()).isEqualTo(1000);
    }

    @Test
    void shouldCreatePrimerWithSmallRiflePrimer() {
        // given & when
        var primer = new Primer(1L, "user123", "Federal", "205M", SMALL_RIFLE,
                               new BigDecimal("60.00"), "USD", 1000);

        // then
        assertThat(primer.primerSize()).isEqualTo(SMALL_RIFLE);
    }

    @Test
    void shouldCreatePrimerWithMagnumPrimer() {
        // given & when
        var primer = new Primer(1L, "user123", "Winchester", "WLR-M", LARGE_RIFLE_MAGNUM,
                               new BigDecimal("70.00"), "CAD", 1000);

        // then
        assertThat(primer.primerSize()).isEqualTo(LARGE_RIFLE_MAGNUM);
    }

    @Test
    void shouldCreatePrimerWithNullId() {
        // given & when
        var primer = new Primer(null, "user123", "Remington", "7 1/2",
                               SMALL_RIFLE, new BigDecimal("55.00"), "USD", 1000);

        // then
        assertThat(primer.id()).isNull();
    }

    @Test
    void equalsAndHashCode_shouldBeEqualForSameBusinessData() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer1).isEqualTo(primer2);
        assertThat(primer1.hashCode()).isEqualTo(primer2.hashCode());
    }

    @Test
    void equalsAndHashCode_shouldBeEqualWithDifferentOwnerId() {
        // given - same business data but different ownerId
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user999", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);

        // then - should be equal because ownerId is excluded from equals
        assertThat(primer1).isEqualTo(primer2);
        assertThat(primer1.hashCode()).isEqualTo(primer2.hashCode());
    }

    @Test
    void equals_shouldHandleStandardContracts() {
        // given
        var primer = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                               new BigDecimal("65.99"), "USD", 1000);

        // then - reflexive, null safety, and type safety
        assertThat(primer).isEqualTo(primer);
        assertThat(primer).isNotEqualTo(null);
        assertThat(primer).isNotEqualTo("Not a primer");
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(2L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentManufacturer() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "Federal", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentType() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "CCI", "BR4", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentPrimerSize() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", SMALL_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCost() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("70.00"), "USD", 1000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCurrency() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "CAD", 1000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentQuantityPerBox() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 5000);

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentBusinessData() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                new BigDecimal("65.99"), "USD", 1000);
        var primer2 = new Primer(2L, "user123", "Federal", "205M", SMALL_RIFLE,
                                new BigDecimal("60.00"), "CAD", 5000);

        // then
        assertThat(primer1.hashCode()).isNotEqualTo(primer2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        // given & when
        var primer = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                               new BigDecimal("65.99"), "USD", 1000);

        // then
        assertThat(primer.toString()).contains("1", "user123", "CCI", "BR2", "LARGE_RIFLE", "65.99", "USD", "1000");
    }
}

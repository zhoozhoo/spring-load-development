package ca.zhoozhoo.loaddev.components.model;

import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE_MAGNUM;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_RIFLE;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Primer} model class.
 * Tests record construction, equality, hashCode, and business logic with JSR-385 and JSR-354 support.
 *
 * @author Zhubin Salehi
 */
class PrimerTest {

    @Test
    void shouldCreatePrimerWithAllFields() {
        // given & when
        var primer = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                               of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer.id()).isEqualTo(1L);
        assertThat(primer.ownerId()).isEqualTo("user123");
        assertThat(primer.manufacturer()).isEqualTo("CCI");
        assertThat(primer.type()).isEqualTo("BR2");
        assertThat(primer.primerSize()).isEqualTo(LARGE_RIFLE);
        assertThat(primer.cost()).isEqualTo(of(65.99, getCurrency("USD")));
        assertThat(primer.quantityPerBox()).isEqualTo(getQuantity(1000, ONE));
    }

    @Test
    void shouldCreatePrimerWithSmallRiflePrimer() {
        // given & when
        var primer = new Primer(1L, "user123", "Federal", "205M", SMALL_RIFLE,
                               of(60.00, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer.primerSize()).isEqualTo(SMALL_RIFLE);
    }

    @Test
    void shouldCreatePrimerWithMagnumPrimer() {
        // given & when
        var primer = new Primer(1L, "user123", "Winchester", "WLR-M", LARGE_RIFLE_MAGNUM,
                               of(70.00, getCurrency("CAD")), getQuantity(1000, ONE));

        // then
        assertThat(primer.primerSize()).isEqualTo(LARGE_RIFLE_MAGNUM);
    }

    @Test
    void shouldCreatePrimerWithNullId() {
        // given & when
        var primer = new Primer(null, "user123", "Remington", "7 1/2",
                               SMALL_RIFLE, of(55.00, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer.id()).isNull();
    }

    @Test
    void equalsAndHashCode_shouldBeEqualForSameBusinessData() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isEqualTo(primer2);
        assertThat(primer1.hashCode()).isEqualTo(primer2.hashCode());
    }

    @Test
    void equalsAndHashCode_shouldBeEqualWithDifferentOwnerId() {
        // given - same business data but different ownerId
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user999", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then - should be equal because ownerId is excluded from equals
        assertThat(primer1).isEqualTo(primer2);
        assertThat(primer1.hashCode()).isEqualTo(primer2.hashCode());
    }

    @Test
    void equals_shouldHandleStandardContracts() {
        // given
        var primer = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                               of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then - reflexive, null safety, and type safety
        assertThat(primer).isEqualTo(primer);
        assertThat(primer).isNotEqualTo(null);
        assertThat(primer).isNotEqualTo("Not a primer");
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(2L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentManufacturer() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "Federal", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentType() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "CCI", "450", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentPrimerSize() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", SMALL_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCost() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(70.00, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCurrency() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("CAD")), getQuantity(1000, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentQuantityPerBox() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(500, ONE));

        // then
        assertThat(primer1).isNotEqualTo(primer2);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentBusinessData() {
        // given
        var primer1 = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                                of(65.99, getCurrency("USD")), getQuantity(1000, ONE));
        var primer2 = new Primer(2L, "user123", "Federal", "205M", SMALL_RIFLE,
                                of(60.00, getCurrency("CAD")), getQuantity(500, ONE));

        // then
        assertThat(primer1.hashCode()).isNotEqualTo(primer2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        // given & when
        var primer = new Primer(1L, "user123", "CCI", "BR2", LARGE_RIFLE,
                               of(65.99, getCurrency("USD")), getQuantity(1000, ONE));

        // then
        assertThat(primer.toString()).contains("1", "user123", "CCI", "BR2", "LARGE_RIFLE", "65.99", "USD", "1000");
    }
}

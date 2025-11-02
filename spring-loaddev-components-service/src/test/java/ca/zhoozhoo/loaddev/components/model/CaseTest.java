package ca.zhoozhoo.loaddev.components.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_RIFLE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Case} model class.
 * Tests record construction, equality, hashCode, and business logic.
 *
 * @author Zhubin Salehi
 */
class CaseTest {

    @Test
    void shouldCreateCaseWithAllFields() {
        // given & when
        var caseItem = new Case(1L, "user123", "Lapua", ".308 Winchester", LARGE_RIFLE, 
                               new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(caseItem.id()).isEqualTo(1L);
        assertThat(caseItem.ownerId()).isEqualTo("user123");
        assertThat(caseItem.manufacturer()).isEqualTo("Lapua");
        assertThat(caseItem.caliber()).isEqualTo(".308 Winchester");
        assertThat(caseItem.primerSize()).isEqualTo(LARGE_RIFLE);
        assertThat(caseItem.cost()).isEqualByComparingTo(new BigDecimal("75.99"));
        assertThat(caseItem.currency()).isEqualTo("USD");
        assertThat(caseItem.quantityPerBox()).isEqualTo(100);
    }

    @Test
    void shouldCreateCaseWithSmallRiflePrimer() {
        // given & when
        var caseItem = new Case(1L, "user123", "Winchester", ".223 Remington",
                               SMALL_RIFLE, new BigDecimal("45.00"), "USD", 50);

        // then
        assertThat(caseItem.primerSize()).isEqualTo(SMALL_RIFLE);
    }

    @Test
    void shouldCreateCaseWithNullId() {
        // given & when
        var caseItem = new Case(null, "user123", "Hornady", "6.5 Creedmoor",
                               LARGE_RIFLE, new BigDecimal("60.00"), "CAD", 100);

        // then
        assertThat(caseItem.id()).isNull();
    }

    @Test
    void equalsAndHashCode_shouldBeEqualForSameBusinessData() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(case1).isEqualTo(case2);
        assertThat(case1.hashCode()).isEqualTo(case2.hashCode());
    }

    @Test
    void equalsAndHashCode_shouldBeEqualWithDifferentOwnerId() {
        // given - same business data but different ownerId
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user999", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then - should be equal because ownerId is excluded from equals
        assertThat(case1).isEqualTo(case2);
        assertThat(case1.hashCode()).isEqualTo(case2.hashCode());
    }

    @Test
    void equals_shouldHandleStandardContracts() {
        // given
        var caseItem = new Case(1L, "user123", "Lapua", ".308 Winchester",
                               LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then - reflexive, null safety, and type safety
        assertThat(caseItem).isEqualTo(caseItem);
        assertThat(caseItem).isNotEqualTo(null);
        assertThat(caseItem).isNotEqualTo("Not a case");
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(2L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentManufacturer() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Winchester", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCaliber() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Lapua", "6.5 Creedmoor",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentPrimerSize() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            SMALL_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCost() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("80.00"), "USD", 100);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCurrency() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "CAD", 100);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentQuantityPerBox() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 50);

        // then
        assertThat(case1).isNotEqualTo(case2);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentBusinessData() {
        // given
        var case1 = new Case(1L, "user123", "Lapua", ".308 Winchester",
                            LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);
        var case2 = new Case(2L, "user123", "Winchester", ".223 Remington",
                            SMALL_RIFLE, new BigDecimal("45.00"), "CAD", 50);

        // then
        assertThat(case1.hashCode()).isNotEqualTo(case2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        // given & when
        var caseItem = new Case(1L, "user123", "Lapua", ".308 Winchester",
                               LARGE_RIFLE, new BigDecimal("75.99"), "USD", 100);

        // then
        assertThat(caseItem.toString()).contains("1", "user123", "Lapua", ".308 Winchester", "LARGE_RIFLE", "75.99", "USD", "100");
    }
}

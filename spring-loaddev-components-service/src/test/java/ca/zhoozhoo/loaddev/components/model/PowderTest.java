package ca.zhoozhoo.loaddev.components.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ca.zhoozhoo.loaddev.components.model.Powder.IMPERIAL;
import static ca.zhoozhoo.loaddev.components.model.Powder.METRIC;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Powder} model class.
 * Tests record construction, equality, hashCode, and business logic.
 *
 * @author Zhubin Salehi
 */
class PowderTest {

    @Test
    void shouldCreatePowderWithAllFields() {
        // given & when
        var powder = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                               new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder.id()).isEqualTo(1L);
        assertThat(powder.ownerId()).isEqualTo("user123");
        assertThat(powder.manufacturer()).isEqualTo("Hodgdon");
        assertThat(powder.type()).isEqualTo("H4350");
        assertThat(powder.measurementUnits()).isEqualTo(METRIC);
        assertThat(powder.cost()).isEqualByComparingTo(new BigDecimal("89.99"));
        assertThat(powder.currency()).isEqualTo("USD");
        assertThat(powder.weightPerContainer()).isEqualTo(1000.0);
    }

    @Test
    void shouldCreatePowderWithImperialUnits() {
        // given & when
        var powder = new Powder(1L, "user123", "IMR", "IMR 4064", IMPERIAL,
                               new BigDecimal("85.50"), "CAD", 8.0);

        // then
        assertThat(powder.measurementUnits()).isEqualTo(IMPERIAL);
    }

    @Test
    void shouldCreatePowderWithNullId() {
        // given & when
        var powder = new Powder(null, "user123", "Alliant", "Reloder 16", METRIC,
                               new BigDecimal("92.00"), "USD", 1000.0);

        // then
        assertThat(powder.id()).isNull();
    }

    @Test
    void equalsAndHashCode_shouldBeEqualForSameBusinessData() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder1).isEqualTo(powder2);
        assertThat(powder1.hashCode()).isEqualTo(powder2.hashCode());
    }

    @Test
    void equalsAndHashCode_shouldBeEqualWithDifferentOwnerId() {
        // given - same business data but different ownerId
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user999", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);

        // then - should be equal because ownerId is excluded from equals
        assertThat(powder1).isEqualTo(powder2);
        assertThat(powder1.hashCode()).isEqualTo(powder2.hashCode());
    }

    @Test
    void equals_shouldHandleStandardContracts() {
        // given
        var powder = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                               new BigDecimal("89.99"), "USD", 1000.0);

        // then - reflexive, null safety, and type safety
        assertThat(powder).isEqualTo(powder);
        assertThat(powder).isNotEqualTo(null);
        assertThat(powder).isNotEqualTo("Not a powder");
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(2L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentManufacturer() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "IMR", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentType() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "Hodgdon", "H4895", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentMeasurementUnits() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "Hodgdon", "H4350", IMPERIAL,
                                new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCost() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("95.00"), "USD", 1000.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCurrency() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "CAD", 1000.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentWeightPerContainer() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 8.0);

        // then
        assertThat(powder1).isNotEqualTo(powder2);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentBusinessData() {
        // given
        var powder1 = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                                new BigDecimal("89.99"), "USD", 1000.0);
        var powder2 = new Powder(2L, "user123", "IMR", "IMR 4064", IMPERIAL,
                                new BigDecimal("85.50"), "CAD", 8.0);

        // then
        assertThat(powder1.hashCode()).isNotEqualTo(powder2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        // given & when
        var powder = new Powder(1L, "user123", "Hodgdon", "H4350", METRIC,
                               new BigDecimal("89.99"), "USD", 1000.0);

        // then
        assertThat(powder.toString()).contains("1", "user123", "Hodgdon", "H4350", "Metric", "89.99", "USD", "1000.0");
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

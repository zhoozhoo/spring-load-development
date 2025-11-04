package ca.zhoozhoo.loaddev.components.model;

import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static systems.uom.ucum.UCUM.POUND;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Propellant} model class.
 * Tests record construction, equality, hashCode, JSR-385 Quantity handling,
 * and JSR-354 MonetaryAmount handling.
 *
 * @author Zhubin Salehi
 */
@DisplayName("Propellant Model Tests")
class PropellantTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create propellant with all fields using grams")
        void shouldCreatePropellantWithAllFieldsUsingGrams() {
            // given & when
            var weight = getQuantity(1000, GRAM);
            var cost = of(89.99, getCurrency("USD"));
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350", cost, weight);

            // then
            assertThat(propellant.id()).isEqualTo(1L);
            assertThat(propellant.ownerId()).isEqualTo("user123");
            assertThat(propellant.manufacturer()).isEqualTo("Hodgdon");
            assertThat(propellant.type()).isEqualTo("H4350");
            assertThat(propellant.cost()).isEqualTo(cost);
            assertThat(propellant.weightPerContainer()).isEqualTo(weight);
        }

        @Test
        @DisplayName("Should create propellant with kilograms")
        void shouldCreatePropellantWithKilograms() {
            // given & when
            var propellant = new Propellant(1L, "user123", "IMR", "IMR 4064",
                                          of(85.50, getCurrency("CAD")), getQuantity(1, KILOGRAM));

            // then
            assertThat(propellant.weightPerContainer()).isEqualTo(getQuantity(1, KILOGRAM));
            assertThat(propellant.weightPerContainer().getUnit()).isEqualTo(KILOGRAM);
        }

        @Test
        @DisplayName("Should create propellant with pounds")
        void shouldCreatePropellantWithPounds() {
            // given & when
            var propellant = new Propellant(1L, "user123", "Alliant", "Reloder 16",
                                          of(92.00, getCurrency("USD")), getQuantity(8, POUND));

            // then
            assertThat(propellant.weightPerContainer()).isEqualTo(getQuantity(8, POUND));
            assertThat(propellant.weightPerContainer().getUnit()).isEqualTo(POUND);
        }

        @Test
        @DisplayName("Should create propellant with null id")
        void shouldCreatePropellantWithNullId() {
            // given & when
            var propellant = new Propellant(null, "user123", "Alliant", "Reloder 16",
                                          of(92.00, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant.id()).isNull();
        }

        @Test
        @DisplayName("Should create propellant with different currencies")
        void shouldCreatePropellantWithDifferentCurrencies() {
            // given
            var weight = getQuantity(1000, GRAM);
            
            // when & then - USD
            var propellantUSD = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                              of(89.99, getCurrency("USD")), weight);
            assertThat(propellantUSD.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");
            
            // when & then - CAD
            var propellantCAD = new Propellant(2L, "user123", "IMR", "IMR 4064",
                                              of(95.50, getCurrency("CAD")), weight);
            assertThat(propellantCAD.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
            
            // when & then - EUR
            var propellantEUR = new Propellant(3L, "user123", "Vihtavuori", "N140",
                                              of(78.75, getCurrency("EUR")), weight);
            assertThat(propellantEUR.cost().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same business data")
        void shouldBeEqualForSameBusinessData() {
            // given
            var weight = getQuantity(1000, GRAM);
            var cost = of(89.99, getCurrency("USD"));
            
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350", cost, weight);
            var propellant2 = new Propellant(1L, "user123", "Hodgdon", "H4350", cost, weight);

            // then
            assertThat(propellant1).isEqualTo(propellant2);
            assertThat(propellant1.hashCode()).isEqualTo(propellant2.hashCode());
        }

        @Test
        @DisplayName("Should be equal with different ownerId")
        void shouldBeEqualWithDifferentOwnerId() {
            // given - same business data but different ownerId
            var weight = getQuantity(1000, GRAM);
            var cost = of(89.99, getCurrency("USD"));
            
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350", cost, weight);
            var propellant2 = new Propellant(1L, "user999", "Hodgdon", "H4350", cost, weight);

            // then - should be equal because ownerId is excluded from equals
            assertThat(propellant1).isEqualTo(propellant2);
            assertThat(propellant1.hashCode()).isEqualTo(propellant2.hashCode());
        }

        @Test
        @DisplayName("Should handle standard equality contracts")
        void shouldHandleStandardEqualityContracts() {
            // given
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));

            // then - reflexive, null safety, and type safety
            assertThat(propellant).isEqualTo(propellant);
            assertThat(propellant).isNotEqualTo(null);
            assertThat(propellant).isNotEqualTo("Not a propellant");
        }

        @Test
        @DisplayName("Should not be equal for different id")
        void shouldNotBeEqualForDifferentId() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(2L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should not be equal for different manufacturer")
        void shouldNotBeEqualForDifferentManufacturer() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(1L, "user123", "IMR", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should not be equal for different type")
        void shouldNotBeEqualForDifferentType() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(1L, "user123", "Hodgdon", "Varget",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should not be equal for different weight value")
        void shouldNotBeEqualForDifferentWeightValue() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(500, GRAM));

            // then
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should not be equal for different weight units")
        void shouldNotBeEqualForDifferentWeightUnits() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1, KILOGRAM));
            var propellant2 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));

            // then - different units means different Quantity objects
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should not be equal for different cost amount")
        void shouldNotBeEqualForDifferentCostAmount() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(95.00, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should not be equal for different currency")
        void shouldNotBeEqualForDifferentCurrency() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("CAD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant1).isNotEqualTo(propellant2);
        }

        @Test
        @DisplayName("Should have different hashCode for different business data")
        void shouldHaveDifferentHashCodeForDifferentBusinessData() {
            // given
            var propellant1 = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                           of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellant2 = new Propellant(2L, "user123", "IMR", "IMR 4064",
                                           of(85.50, getCurrency("CAD")), getQuantity(8, POUND));

            // then
            assertThat(propellant1.hashCode()).isNotEqualTo(propellant2.hashCode());
        }
    }

    @Nested
    @DisplayName("JSR-385 Quantity Tests")
    class QuantityTests {

        @Test
        @DisplayName("Should handle weight conversions between units")
        void shouldHandleWeightConversionsBetweenUnits() {
            // given
            var weightInPounds = getQuantity(1, POUND);
            var weightInGrams = weightInPounds.to(GRAM);
            
            // when
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(89.99, getCurrency("USD")), weightInGrams);

            // then
            assertThat(propellant.weightPerContainer().getUnit()).isEqualTo(GRAM);
            assertThat(propellant.weightPerContainer().getValue().doubleValue())
                .isCloseTo(453.59, org.assertj.core.data.Offset.offset(1.0));
        }

        @Test
        @DisplayName("Should preserve weight unit as specified")
        void shouldPreserveWeightUnitAsSpecified() {
            // given & when
            var propellantGrams = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                                of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));
            var propellantKg = new Propellant(2L, "user123", "IMR", "IMR 4064",
                                             of(85.50, getCurrency("CAD")), getQuantity(1, KILOGRAM));
            var propellantLbs = new Propellant(3L, "user123", "Alliant", "Reloder 16",
                                              of(92.00, getCurrency("USD")), getQuantity(8, POUND));

            // then
            assertThat(propellantGrams.weightPerContainer().getUnit()).isEqualTo(GRAM);
            assertThat(propellantKg.weightPerContainer().getUnit()).isEqualTo(KILOGRAM);
            assertThat(propellantLbs.weightPerContainer().getUnit()).isEqualTo(POUND);
        }

        @Test
        @DisplayName("Should handle fractional weight values")
        void shouldHandleFractionalWeightValues() {
            // given & when
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(89.99, getCurrency("USD")), getQuantity(0.454, KILOGRAM));

            // then
            assertThat(propellant.weightPerContainer().getValue().doubleValue())
                .isCloseTo(0.454, org.assertj.core.data.Offset.offset(0.001));
        }
    }

    @Nested
    @DisplayName("JSR-354 MonetaryAmount Tests")
    class MonetaryAmountTests {

        @Test
        @DisplayName("Should handle monetary amount with different currencies")
        void shouldHandleMonetaryAmountWithDifferentCurrencies() {
            // given
            var weight = getQuantity(1000, GRAM);
            
            // when & then
            var propellantUSD = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                             of(89.99, getCurrency("USD")), weight);
            assertThat(propellantUSD.cost().getNumber().doubleValue()).isCloseTo(89.99,
                org.assertj.core.data.Offset.offset(0.001));
            assertThat(propellantUSD.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");

            var propellantCAD = new Propellant(2L, "user123", "IMR", "IMR 4064",
                                             of(95.50, getCurrency("CAD")), weight);
            assertThat(propellantCAD.cost().getNumber().doubleValue()).isCloseTo(95.50,
                org.assertj.core.data.Offset.offset(0.001));
            assertThat(propellantCAD.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
        }

        @Test
        @DisplayName("Should preserve monetary precision")
        void shouldPreserveMonetaryPrecision() {
            // given & when
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(89.999, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant.cost().getNumber().doubleValue()).isCloseTo(89.999,
                org.assertj.core.data.Offset.offset(0.0001));
        }

        @Test
        @DisplayName("Should handle zero cost")
        void shouldHandleZeroCost() {
            // given & when
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(0, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant.cost().getNumber().doubleValue()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle high precision currency amounts")
        void shouldHandleHighPrecisionCurrencyAmounts() {
            // given & when
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(123.456789, getCurrency("USD")), getQuantity(1000, GRAM));

            // then
            assertThat(propellant.cost().getNumber().doubleValue()).isCloseTo(123.456789,
                org.assertj.core.data.Offset.offset(0.000001));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all fields in toString")
        void shouldContainAllFieldsInToString() {
            // given
            var propellant = new Propellant(1L, "user123", "Hodgdon", "H4350",
                                          of(89.99, getCurrency("USD")), getQuantity(1000, GRAM));

            // when
            var toString = propellant.toString();

            // then
            assertThat(toString).contains("1");
            assertThat(toString).contains("user123");
            assertThat(toString).contains("Hodgdon");
            assertThat(toString).contains("H4350");
        }
    }
}

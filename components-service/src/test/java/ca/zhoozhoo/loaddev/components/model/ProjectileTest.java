package ca.zhoozhoo.loaddev.components.model;

import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static systems.uom.ucum.UCUM.GRAIN;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Projectile} model class.
 * Tests record construction, equality, hashCode, JSR-385 Quantity handling,
 * and JSR-354 MonetaryAmount handling.
 *
 * @author Zhubin Salehi
 */
@DisplayName("Projectile Model Tests")
class ProjectileTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create projectile with all fields using grains")
        void shouldCreateProjectileWithAllFieldsUsingGrains() {
            // given & when
            var weight = getQuantity(168, GRAIN);
            var cost = of(45.99, getCurrency("USD"));
            var projectile = new Projectile(1L, "user123", "Hornady", weight, "BTHP", cost, 100);

            // then
            assertThat(projectile.id()).isEqualTo(1L);
            assertThat(projectile.ownerId()).isEqualTo("user123");
            assertThat(projectile.manufacturer()).isEqualTo("Hornady");
            assertThat(projectile.weight()).isEqualTo(weight);
            assertThat(projectile.type()).isEqualTo("BTHP");
            assertThat(projectile.cost()).isEqualTo(cost);
            assertThat(projectile.quantityPerBox()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should create projectile with grams")
        void shouldCreateProjectileWithGrams() {
            // given & when
            var projectile = new Projectile(1L, "user123", "Sierra", getQuantity(10.89, GRAM), "MatchKing", 
                                          of(52.50, getCurrency("CAD")), 100);

            // then
            assertThat(projectile.weight()).isEqualTo(getQuantity(10.89, GRAM));
            assertThat(projectile.weight().getUnit()).isEqualTo(GRAM);
        }

        @Test
        @DisplayName("Should create projectile with kilograms")
        void shouldCreateProjectileWithKilograms() {
            // given & when
            var projectile = new Projectile(1L, "user123", "Nosler", getQuantity(0.01089, KILOGRAM), "Ballistic Tip",
                                          of(38.75, getCurrency("EUR")), 50);

            // then
            assertThat(projectile.weight()).isEqualTo(getQuantity(0.01089, KILOGRAM));
            assertThat(projectile.weight().getUnit()).isEqualTo(KILOGRAM);
        }

        @Test
        @DisplayName("Should create projectile with null id")
        void shouldCreateProjectileWithNullId() {
            // given & when
            var projectile = new Projectile(null, "user123", "Nosler", getQuantity(150, GRAIN), "Ballistic Tip",
                                          of(40.00, getCurrency("USD")), 50);

            // then
            assertThat(projectile.id()).isNull();
        }

        @Test
        @DisplayName("Should create projectile with different currencies")
        void shouldCreateProjectileWithDifferentCurrencies() {
            // given
            var weight = getQuantity(168, GRAIN);
            
            // when & then - USD
            var projectileUSD = new Projectile(1L, "user123", "Hornady", weight, "BTHP", 
                                               of(45.99, getCurrency("USD")), 100);
            assertThat(projectileUSD.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");
            
            // when & then - CAD
            var projectileCAD = new Projectile(2L, "user123", "Sierra", weight, "MatchKing",
                                               of(62.50, getCurrency("CAD")), 100);
            assertThat(projectileCAD.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
            
            // when & then - EUR
            var projectileEUR = new Projectile(3L, "user123", "Lapua", weight, "Scenar",
                                               of(55.75, getCurrency("EUR")), 100);
            assertThat(projectileEUR.cost().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same business data")
        void shouldBeEqualForSameBusinessData() {
            // given
            var weight = getQuantity(168, GRAIN);
            var cost = of(45.99, getCurrency("USD"));
            
            var projectile1 = new Projectile(1L, "user123", "Hornady", weight, "BTHP", cost, 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", weight, "BTHP", cost, 100);

            // then
            assertThat(projectile1).isEqualTo(projectile2);
            assertThat(projectile1.hashCode()).isEqualTo(projectile2.hashCode());
        }

        @Test
        @DisplayName("Should be equal with different ownerId")
        void shouldBeEqualWithDifferentOwnerId() {
            // given - same business data but different ownerId
            var weight = getQuantity(168, GRAIN);
            var cost = of(45.99, getCurrency("USD"));
            
            var projectile1 = new Projectile(1L, "user123", "Hornady", weight, "BTHP", cost, 100);
            var projectile2 = new Projectile(1L, "user999", "Hornady", weight, "BTHP", cost, 100);

            // then - should be equal because ownerId is excluded from equals
            assertThat(projectile1).isEqualTo(projectile2);
            assertThat(projectile1.hashCode()).isEqualTo(projectile2.hashCode());
        }

        @Test
        @DisplayName("Should handle standard equality contracts")
        void shouldHandleStandardEqualityContracts() {
            // given
            var projectile = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                          of(45.99, getCurrency("USD")), 100);

            // then - reflexive, null safety, and type safety
            assertThat(projectile).isEqualTo(projectile);
            assertThat(projectile).isNotEqualTo(null);
            assertThat(projectile).isNotEqualTo("Not a projectile");
        }

        @Test
        @DisplayName("Should not be equal for different id")
        void shouldNotBeEqualForDifferentId() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(2L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different manufacturer")
        void shouldNotBeEqualForDifferentManufacturer() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Sierra", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different weight value")
        void shouldNotBeEqualForDifferentWeightValue() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", getQuantity(175, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different weight units")
        void shouldNotBeEqualForDifferentWeightUnits() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", getQuantity(10.89, GRAM), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);

            // then - different units means different Quantity objects
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different type")
        void shouldNotBeEqualForDifferentType() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "ELD-M",
                                           of(45.99, getCurrency("USD")), 100);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different cost amount")
        void shouldNotBeEqualForDifferentCostAmount() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(50.00, getCurrency("USD")), 100);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different currency")
        void shouldNotBeEqualForDifferentCurrency() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("CAD")), 100);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should not be equal for different quantity per box")
        void shouldNotBeEqualForDifferentQuantityPerBox() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                           of(45.99, getCurrency("USD")), 50);

            // then
            assertThat(projectile1).isNotEqualTo(projectile2);
        }

        @Test
        @DisplayName("Should have different hashCode for different business data")
        void shouldHaveDifferentHashCodeForDifferentBusinessData() {
            // given
            var projectile1 = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), 
                                           "BTHP", of(45.99, getCurrency("USD")), 100);
            var projectile2 = new Projectile(2L, "user123", "Sierra", getQuantity(175, GRAIN),
                                           "MatchKing", of(52.50, getCurrency("CAD")), 50);

            // then
            assertThat(projectile1.hashCode()).isNotEqualTo(projectile2.hashCode());
        }
    }

    @Nested
    @DisplayName("JSR-385 Quantity Tests")
    class QuantityTests {

        @Test
        @DisplayName("Should handle weight conversions between units")
        void shouldHandleWeightConversionsBetweenUnits() {
            // given
            var weightInGrains = getQuantity(168, GRAIN);
            var weightInGrams = weightInGrains.to(GRAM);
            
            // when
            var projectile = new Projectile(1L, "user123", "Hornady", weightInGrams, "BTHP", 
                                          of(45.99, getCurrency("USD")), 100);

            // then
            assertThat(projectile.weight().getUnit()).isEqualTo(GRAM);
            assertThat(projectile.weight().getValue().doubleValue()).isCloseTo(10.89, 
                org.assertj.core.data.Offset.offset(0.01));
        }

        @Test
        @DisplayName("Should preserve weight unit as specified")
        void shouldPreserveWeightUnitAsSpecified() {
            // given & when
            var projectileGrains = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN),
                                                 "BTHP", of(45.99, getCurrency("USD")), 100);
            var projectileGrams = new Projectile(2L, "user123", "Sierra", getQuantity(10.89, GRAM),
                                                "MatchKing", of(52.50, getCurrency("CAD")), 100);

            // then
            assertThat(projectileGrains.weight().getUnit()).isEqualTo(GRAIN);
            assertThat(projectileGrams.weight().getUnit()).isEqualTo(GRAM);
        }
    }

    @Nested
    @DisplayName("JSR-354 MonetaryAmount Tests")
    class MonetaryAmountTests {

        @Test
        @DisplayName("Should handle monetary amount with different currencies")
        void shouldHandleMonetaryAmountWithDifferentCurrencies() {
            // given
            var weight = getQuantity(168, GRAIN);
            
            // when & then
            var projectileUSD = new Projectile(1L, "user123", "Hornady", weight, "BTHP",
                                              of(45.99, getCurrency("USD")), 100);
            assertThat(projectileUSD.cost().getNumber().doubleValue()).isCloseTo(45.99,
                org.assertj.core.data.Offset.offset(0.001));
            assertThat(projectileUSD.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");

            var projectileCAD = new Projectile(2L, "user123", "Sierra", weight, "MatchKing",
                                              of(62.50, getCurrency("CAD")), 100);
            assertThat(projectileCAD.cost().getNumber().doubleValue()).isCloseTo(62.50,
                org.assertj.core.data.Offset.offset(0.001));
            assertThat(projectileCAD.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
        }

        @Test
        @DisplayName("Should preserve monetary precision")
        void shouldPreserveMonetaryPrecision() {
            // given & when
            var projectile = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                          of(45.999, getCurrency("USD")), 100);

            // then
            assertThat(projectile.cost().getNumber().doubleValue()).isCloseTo(45.999,
                org.assertj.core.data.Offset.offset(0.0001));
        }

        @Test
        @DisplayName("Should handle zero cost")
        void shouldHandleZeroCost() {
            // given & when
            var projectile = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                          of(0, getCurrency("USD")), 100);

            // then
            assertThat(projectile.cost().getNumber().doubleValue()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all fields in toString")
        void shouldContainAllFieldsInToString() {
            // given
            var projectile = new Projectile(1L, "user123", "Hornady", getQuantity(168, GRAIN), "BTHP",
                                          of(45.99, getCurrency("USD")), 100);

            // when
            var toString = projectile.toString();

            // then
            assertThat(toString).contains("1");
            assertThat(toString).contains("user123");
            assertThat(toString).contains("Hornady");
            assertThat(toString).contains("BTHP");
            assertThat(toString).contains("100");
        }
    }
}

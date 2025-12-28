package ca.zhoozhoo.loaddev.components.dao;

import static java.util.UUID.randomUUID;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static reactor.core.publisher.Flux.just;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.POUND;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.model.Propellant;

/**
 * Integration tests for {@link PropellantRepository}.
 * <p>
 * Tests repository operations including JSR-385 Quantity and JSR-354 MonetaryAmount
 * persistence to/from PostgreSQL JSONB columns.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class PropellantRepositoryTest {

    @Autowired
    private PropellantRepository propellantRepository;

    @BeforeEach
    void setup() {
        propellantRepository.deleteAll().block();
    }

    private Propellant createTestPropellant(String ownerId) {
        return new Propellant(
                null,
                ownerId,
                "Hodgdon",
                "H4350",
                of(45.99, getCurrency("USD")),
                getQuantity(1.0, POUND));
    }

    @Test
    void savePropellant() {
        var userId = randomUUID().toString();

        create(propellantRepository.save(createTestPropellant(userId)))
                .assertNext(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(45.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isCloseTo(1.0, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weightPerContainer().getUnit()).isEqualTo(POUND);
                })
                .verifyComplete();
    }

    @Test
    void findPropellantById() {
        var userId = randomUUID().toString();
        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        create(propellantRepository.findByIdAndOwnerId(savedPropellant.id(), userId))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPropellant.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(45.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isCloseTo(1.0, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weightPerContainer().getUnit()).isEqualTo(POUND);
                })
                .verifyComplete();
    }

    @Test
    void updatePropellant() {
        var userId = randomUUID().toString();
        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        var updatedPropellant = new Propellant(
                savedPropellant.id(),
                userId,
                "Vihtavuori",
                "N550",
                of(299.99, getCurrency("CAD")),
                getQuantity(8.0, POUND));

        create(propellantRepository.save(updatedPropellant))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPropellant.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Vihtavuori");
                    assertThat(p.type()).isEqualTo("N550");
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(299.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isCloseTo(8.0, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weightPerContainer().getUnit()).isEqualTo(POUND);
                })
                .verifyComplete();
    }

    @Test
    void deletePropellant() {
        var userId = randomUUID().toString();
        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        create(propellantRepository.delete(savedPropellant)).verifyComplete();
        create(propellantRepository.findByIdAndOwnerId(savedPropellant.id(), userId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var userId = randomUUID().toString();
        var propellant1 = createTestPropellant(userId);
        var propellant2 = new Propellant(
                null,
                userId,
                "IMR",
                "4064",
                of(54.99, getCurrency("USD")),
                getQuantity(1.0, POUND));

        propellantRepository.saveAll(just(propellant1, propellant2)).blockLast();

        create(propellantRepository.findAllByOwnerId(userId))
                .expectNextMatches(p -> p.manufacturer().equals("Hodgdon"))
                .expectNextMatches(p -> p.manufacturer().equals("IMR"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQuery() {
        var ownerId = randomUUID().toString();
        propellantRepository.saveAll(just(createTestPropellant(ownerId))).blockLast();

        create(propellantRepository.searchByOwnerIdAndQuery(ownerId, "Hodgdon H4350"))
                .expectNextMatches(p -> p.manufacturer().equals("Hodgdon"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQueryNegative() {
        var ownerId = randomUUID().toString();
        propellantRepository.saveAll(just(createTestPropellant(ownerId))).blockLast();

        create(propellantRepository.searchByOwnerIdAndQuery(ownerId, "H4350 Varget"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void persistQuantityWithDifferentUnits() {
        var userId = randomUUID().toString();
        var propellantKg = new Propellant(
                null,
                userId,
                "Alliant",
                "Reloder 26",
                of(89.99, getCurrency("USD")),
                getQuantity(0.454, KILOGRAM));

        create(propellantRepository.save(propellantKg))
                .assertNext(p -> {
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isCloseTo(0.454, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weightPerContainer().getUnit()).isEqualTo(KILOGRAM);
                })
                .verifyComplete();
    }

    @Test
    void persistMonetaryAmountWithDifferentCurrencies() {
        var userId = randomUUID().toString();
        var propellantEUR = new Propellant(
                null,
                userId,
                "Vectan",
                "A0",
                of(65.50, getCurrency("EUR")),
                getQuantity(0.5, KILOGRAM));

        create(propellantRepository.save(propellantEUR))
                .assertNext(p -> {
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(65.50, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("EUR");
                })
                .verifyComplete();
    }

    @Test
    void verifyMultiTenantIsolation() {
        var user1 = randomUUID().toString();
        var user2 = randomUUID().toString();
        
        propellantRepository.save(createTestPropellant(user1)).block();
        propellantRepository.save(createTestPropellant(user2)).block();

        create(propellantRepository.findAllByOwnerId(user1))
                .expectNextCount(1)
                .verifyComplete();

        create(propellantRepository.findAllByOwnerId(user2))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void persistFractionalWeightValues() {
        var userId = randomUUID().toString();
        var propellant = new Propellant(
                null,
                userId,
                "Accurate",
                "2230",
                of(42.50, getCurrency("USD")),
                getQuantity(0.5, POUND));

        create(propellantRepository.save(propellant))
                .assertNext(p -> {
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isCloseTo(0.5, 
                        org.assertj.core.data.Offset.offset(0.01));
                })
                .verifyComplete();
    }

    @Test
    void persistHighPrecisionMonetaryAmount() {
        var userId = randomUUID().toString();
        var propellant = new Propellant(
                null,
                userId,
                "Winchester",
                "296",
                of(39.995, getCurrency("USD")),
                getQuantity(1.0, POUND));

        create(propellantRepository.save(propellant))
                .assertNext(p -> {
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(39.995, 
                        org.assertj.core.data.Offset.offset(0.001));
                })
                .verifyComplete();
    }
}

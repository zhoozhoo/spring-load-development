package ca.zhoozhoo.loaddev.components.dao;

import static java.util.UUID.randomUUID;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static reactor.core.publisher.Flux.just;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.GRAIN;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.model.Projectile;

/**
 * Integration tests for {@link ProjectileRepository}.
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
class ProjectileRepositoryTest {

    @Autowired
    private ProjectileRepository projectileRepository;

    @BeforeEach
    void setup() {
        projectileRepository.deleteAll().block();
    }

    private Projectile createTestProjectile(String ownerId) {
        return new Projectile(
                null,
                ownerId,
                "Hornady",
                getQuantity(168, GRAIN),
                "ELD-Match",
                of(45.99, getCurrency("USD")),
                100);
    }

    @Test
    void saveProjectile() {
        var userId = randomUUID().toString();

        create(projectileRepository.save(createTestProjectile(userId)))
                .assertNext(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hornady");
                    assertThat(p.weight().getValue().doubleValue()).isCloseTo(168.0, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weight().getUnit()).isEqualTo(GRAIN);
                    assertThat(p.type()).isEqualTo("ELD-Match");
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(45.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");
                    assertThat(p.quantityPerBox()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void findProjectileById() {
        var userId = randomUUID().toString();
        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        create(projectileRepository.findByIdAndOwnerId(savedProjectile.id(), userId))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedProjectile.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hornady");
                    assertThat(p.weight().getValue().doubleValue()).isCloseTo(168.0, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weight().getUnit()).isEqualTo(GRAIN);
                    assertThat(p.type()).isEqualTo("ELD-Match");
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(45.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("USD");
                    assertThat(p.quantityPerBox()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void updateProjectile() {
        var userId = randomUUID().toString();
        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        var updatedProjectile = new Projectile(
                savedProjectile.id(),
                userId,
                "Sierra",
                getQuantity(175, GRAIN),
                "MatchKing",
                of(49.99, getCurrency("CAD")),
                50);

        create(projectileRepository.save(updatedProjectile))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedProjectile.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Sierra");
                    assertThat(p.weight().getValue().doubleValue()).isCloseTo(175.0, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weight().getUnit()).isEqualTo(GRAIN);
                    assertThat(p.type()).isEqualTo("MatchKing");
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(49.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
                    assertThat(p.quantityPerBox()).isEqualTo(50);
                })
                .verifyComplete();
    }

    @Test
    void deleteProjectile() {
        var userId = randomUUID().toString();
        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        create(projectileRepository.delete(savedProjectile)).verifyComplete();
        create(projectileRepository.findByIdAndOwnerId(savedProjectile.id(), userId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findByIdAndOwnerId() {
        var userId = randomUUID().toString();
        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        create(projectileRepository.findByIdAndOwnerId(savedProjectile.id(), savedProjectile.ownerId()))
                .expectNextMatches(p -> p.id().equals(savedProjectile.id()))
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var userId = randomUUID().toString();
        var projectile1 = createTestProjectile(userId);
        var projectile2 = new Projectile(
                null,
                userId,
                "Berger",
                getQuantity(155, GRAIN),
                "Hybrid Target",
                of(54.99, getCurrency("USD")),
                100);

        projectileRepository.saveAll(just(projectile1, projectile2)).blockLast();

        create(projectileRepository.findAllByOwnerId(userId))
                .expectNextMatches(p -> p.manufacturer().equals("Hornady"))
                .expectNextMatches(p -> p.manufacturer().equals("Berger"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQuery() {
        var userId = randomUUID().toString();
        projectileRepository.saveAll(just(createTestProjectile(userId))).blockLast();

        create(projectileRepository.searchByOwnerIdAndQuery(userId, "Hornady ELD 168"))
                .expectNextMatches(p -> p.manufacturer().equals("Hornady"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQueryNegative() {
        var userId = randomUUID().toString();
        projectileRepository.saveAll(just(createTestProjectile(userId))).blockLast();

        create(projectileRepository.searchByOwnerIdAndQuery(userId, "Sierra MatchKing"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void persistQuantityWithDifferentUnits() {
        var userId = randomUUID().toString();
        var projectileGrams = new Projectile(
                null,
                userId,
                "Nosler",
                getQuantity(10.89, GRAM),
                "Ballistic Tip",
                of(39.99, getCurrency("USD")),
                50);

        create(projectileRepository.save(projectileGrams))
                .assertNext(p -> {
                    assertThat(p.weight().getValue().doubleValue()).isCloseTo(10.89, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.weight().getUnit()).isEqualTo(GRAM);
                })
                .verifyComplete();
    }

    @Test
    void persistMonetaryAmountWithDifferentCurrencies() {
        var userId = randomUUID().toString();
        var projectileCAD = new Projectile(
                null,
                userId,
                "Barnes",
                getQuantity(150, GRAIN),
                "TTSX",
                of(59.99, getCurrency("CAD")),
                50);

        create(projectileRepository.save(projectileCAD))
                .assertNext(p -> {
                    assertThat(p.cost().getNumber().doubleValue()).isCloseTo(59.99, 
                        org.assertj.core.data.Offset.offset(0.01));
                    assertThat(p.cost().getCurrency().getCurrencyCode()).isEqualTo("CAD");
                })
                .verifyComplete();
    }

    @Test
    void verifyMultiTenantIsolation() {
        var user1 = randomUUID().toString();
        var user2 = randomUUID().toString();
        
        projectileRepository.save(createTestProjectile(user1)).block();
        projectileRepository.save(createTestProjectile(user2)).block();

        create(projectileRepository.findAllByOwnerId(user1))
                .expectNextCount(1)
                .verifyComplete();

        create(projectileRepository.findAllByOwnerId(user2))
                .expectNextCount(1)
                .verifyComplete();
    }
}

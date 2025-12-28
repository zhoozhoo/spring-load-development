package ca.zhoozhoo.loaddev.components.dao;

import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE_MAGNUM;
import static java.util.UUID.randomUUID;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static reactor.core.publisher.Flux.just;
import static reactor.test.StepVerifier.create;
import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.model.Primer;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class PrimerRepositoryTest {

    @Autowired
    private PrimerRepository primerRepository;

    @BeforeEach
    void setup() {
        primerRepository.deleteAll().block();
    }

    private Primer createTestPrimer(String ownerId) {
        return new Primer(
                null,
                ownerId,
                "CCI",
                "BR-4",
                LARGE_RIFLE,
                of(89.99, getCurrency("CAD")),
                getQuantity(1000, ONE));
    }

    @Test
    void savePrimer() {
        var userId = randomUUID().toString();

        create(primerRepository.save(createTestPrimer(userId)))
                .assertNext(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("CCI");
                    assertThat(p.type()).isEqualTo("BR-4");
                    assertThat(p.primerSize()).isEqualTo(LARGE_RIFLE);
                    assertThat(p.cost().getNumber().doubleValue()).isEqualTo(89.99);
                    assertThat(p.cost().getCurrency()).isEqualTo(getCurrency("CAD"));
                    assertThat(p.quantityPerBox().getValue().doubleValue()).isEqualTo(1000.0);
                    assertThat(p.quantityPerBox().getUnit()).isEqualTo(ONE);
                })
                .verifyComplete();
    }

    @Test
    void findPrimerById() {
        var userId = randomUUID().toString();
        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        create(primerRepository.findByIdAndOwnerId(savedPrimer.id(), userId))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPrimer.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("CCI");
                    assertThat(p.type()).isEqualTo("BR-4");
                    assertThat(p.primerSize()).isEqualTo(LARGE_RIFLE);
                    assertThat(p.cost().getNumber().doubleValue()).isEqualTo(89.99);
                    assertThat(p.cost().getCurrency()).isEqualTo(getCurrency("CAD"));
                    assertThat(p.quantityPerBox().getValue().doubleValue()).isEqualTo(1000.0);
                    assertThat(p.quantityPerBox().getUnit()).isEqualTo(ONE);
                })
                .verifyComplete();
    }

    @Test
    void updatePrimer() {
        var userId = randomUUID().toString();
        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        var updatedPrimer = new Primer(
                savedPrimer.id(),
                userId,
                "Federal",
                "205M",
                LARGE_RIFLE_MAGNUM,
                of(99.99, getCurrency("CAD")),
                getQuantity(500, ONE));

        create(primerRepository.save(updatedPrimer))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPrimer.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Federal");
                    assertThat(p.type()).isEqualTo("205M");
                    assertThat(p.primerSize()).isEqualTo(LARGE_RIFLE_MAGNUM);
                    assertThat(p.cost().getNumber().doubleValue()).isEqualTo(99.99);
                    assertThat(p.cost().getCurrency()).isEqualTo(getCurrency("CAD"));
                    assertThat(p.quantityPerBox().getValue().doubleValue()).isEqualTo(500.0);
                    assertThat(p.quantityPerBox().getUnit()).isEqualTo(ONE);
                })
                .verifyComplete();
    }

    @Test
    void deletePrimer() {
        var userId = randomUUID().toString();
        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        create(primerRepository.delete(savedPrimer)).verifyComplete();

        create(primerRepository.findByIdAndOwnerId(savedPrimer.id(), userId)).expectNextCount(0).verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var userId = randomUUID().toString();
        var primer1 = createTestPrimer(userId);
        var primer2 = new Primer(
                null,
                userId,
                "Winchester",
                "WLR",
                LARGE_RIFLE,
                of(79.99, getCurrency("CAD")),
                getQuantity(1000, ONE));

        primerRepository.saveAll(just(primer1, primer2)).blockLast();

        create(primerRepository.findAllByOwnerId(userId))
                .expectNextMatches(p -> p.manufacturer().equals("CCI"))
                .expectNextMatches(p -> p.manufacturer().equals("Winchester"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQuery() {
        var ownerId = randomUUID().toString();

        primerRepository.saveAll(just(createTestPrimer(ownerId))).blockLast();

        create(primerRepository.searchByOwnerIdAndQuery(ownerId, "CCI BR-4"))
                .expectNextMatches(pp -> pp.manufacturer().equals("CCI"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQueryNegative() {
        var ownerId = randomUUID().toString();

        primerRepository.saveAll(just(createTestPrimer(ownerId))).blockLast();

        create(primerRepository.searchByOwnerIdAndQuery(ownerId, "CCI 45o"))
                .expectNextCount(0)
                .verifyComplete();
    }
}

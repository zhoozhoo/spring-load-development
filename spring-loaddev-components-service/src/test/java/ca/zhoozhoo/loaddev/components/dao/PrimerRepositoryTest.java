package ca.zhoozhoo.loaddev.components.dao;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.core.publisher.Flux.just;
import static reactor.test.StepVerifier.create;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.model.Primer;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;

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
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("89.99"),
                "CAD",
                1000);
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
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.quantityPerBox()).isEqualTo(1000);
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
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.quantityPerBox()).isEqualTo(1000);
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
                PrimerSize.LARGE_RIFLE_MAGNUM,
                new BigDecimal("99.99"),
                "CAD",
                500);

        create(primerRepository.save(updatedPrimer))
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPrimer.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Federal");
                    assertThat(p.type()).isEqualTo("205M");
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE_MAGNUM);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("99.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.quantityPerBox()).isEqualTo(500);
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
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("79.99"),
                "CAD",
                1000);

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

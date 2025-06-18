package ca.zhoozhoo.loaddev.components.dao;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
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
import reactor.core.publisher.Flux;

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
                "USD",
                1000);
    }

    @Test
    void savePrimer() {
        var userId = randomUUID().toString();
        var savedPrimer = primerRepository.save(createTestPrimer(userId));

        create(savedPrimer)
                .assertNext(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("CCI");
                    assertThat(p.type()).isEqualTo("BR-4");
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(p.currency()).isEqualTo("USD");
                    assertThat(p.quantityPerBox()).isEqualTo(1000);
                })
                .verifyComplete();
    }

    @Test
    void findPrimerById() {
        var userId = randomUUID().toString();
        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();
        var foundPrimer = primerRepository.findByIdAndOwnerId(savedPrimer.id(), userId);

        create(foundPrimer)
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPrimer.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("CCI");
                    assertThat(p.type()).isEqualTo("BR-4");
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(p.currency()).isEqualTo("USD");
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
                "USD",
                500);

        var result = primerRepository.save(updatedPrimer);

        create(result)
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPrimer.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Federal");
                    assertThat(p.type()).isEqualTo("205M");
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE_MAGNUM);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("99.99"));
                    assertThat(p.currency()).isEqualTo("USD");
                    assertThat(p.quantityPerBox()).isEqualTo(500);
                })
                .verifyComplete();
    }

    @Test
    void deletePrimer() {
        var userId = randomUUID().toString();
        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        var deletedPrimer = primerRepository.delete(savedPrimer);

        create(deletedPrimer)
                .verifyComplete();

        var foundPrimer = primerRepository.findByIdAndOwnerId(savedPrimer.id(), userId);

        create(foundPrimer)
                .expectNextCount(0)
                .verifyComplete();
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
                "USD",
                1000);

        primerRepository.saveAll(Flux.just(primer1, primer2)).blockLast();

        var result = primerRepository.findAllByOwnerId(userId);

        create(result)
                .expectNextMatches(p -> p.manufacturer().equals("CCI"))
                .expectNextMatches(p -> p.manufacturer().equals("Winchester"))
                .verifyComplete();
    }
}

package ca.zhoozhoo.loaddev.components.dao;

import static ca.zhoozhoo.loaddev.components.model.Powder.IMPERIAL;
import static ca.zhoozhoo.loaddev.components.model.Powder.METRIC;
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
import ca.zhoozhoo.loaddev.components.model.Powder;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class PowderRepositoryTest {

    @Autowired
    private PowderRepository powderRepository;

    @BeforeEach
    void setup() {
        powderRepository.deleteAll().block();
    }

    private Powder createTestPowder(String ownerId) {
        return new Powder(
                null,
                ownerId,
                "Hodgdon",
                "H4350",
                IMPERIAL,
                new BigDecimal("45.99"),
                "USD",
                1.0);
    }

    @Test
    void savePowder() {
        var userId = randomUUID().toString();
        var savedPowder = powderRepository.save(createTestPowder(userId));

        create(savedPowder)
                .assertNext(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("45.99"));
                    assertThat(p.currency()).isEqualTo("USD");
                    assertThat(p.weightPerContainer()).isEqualTo(1.0);
                })
                .verifyComplete();
    }

    @Test
    void findPowderById() {
        var userId = randomUUID().toString();
        var savedPowder = powderRepository.save(createTestPowder(userId)).block();
        var foundPowder = powderRepository.findByIdAndOwnerId(savedPowder.id(), userId);

        create(foundPowder)
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPowder.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("45.99"));
                    assertThat(p.currency()).isEqualTo("USD");
                    assertThat(p.weightPerContainer()).isEqualTo(1.0);
                })
                .verifyComplete();
    }

    @Test
    void updatePowder() {
        var userId = randomUUID().toString();
        var savedPowder = powderRepository.save(createTestPowder(userId)).block();

        var updatedPowder = new Powder(
                savedPowder.id(),
                userId,
                "Hodgdon",
                "H4350",
                IMPERIAL,
                new BigDecimal("299.99"),
                "USD",
                8.0);

        var result = powderRepository.save(updatedPowder);

        create(result)
                .assertNext(p -> {
                    assertThat(p.id()).isEqualTo(savedPowder.id());
                    assertThat(p.ownerId()).isEqualTo(userId);
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("299.99"));
                    assertThat(p.currency()).isEqualTo("USD");
                    assertThat(p.weightPerContainer()).isEqualTo(8.0);
                })
                .verifyComplete();
    }

    @Test
    void deletePowder() {
        var userId = randomUUID().toString();
        var savedPowder = powderRepository
                .save(createTestPowder(userId))
                .block();

        var deletedPowder = powderRepository.delete(savedPowder);

        create(deletedPowder)
                .verifyComplete();

        var foundPowder = powderRepository.findByIdAndOwnerId(savedPowder.id(), userId);

        create(foundPowder)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var userId = randomUUID().toString();
        var powder1 = createTestPowder(userId);
        var powder2 = new Powder(
                null,
                userId,
                "Vihtavuori",
                "N550",
                METRIC,
                new BigDecimal("54.99"),
                "USD",
                1.0);

        powderRepository.saveAll(Flux.just(powder1, powder2)).blockLast();

        var result = powderRepository.findAllByOwnerId(userId);

        create(result)
                .expectNextMatches(p -> p.manufacturer().equals("Hodgdon"))
                .expectNextMatches(p -> p.manufacturer().equals("Vihtavuori"))
                .verifyComplete();
    }
}

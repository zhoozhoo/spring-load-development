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
import ca.zhoozhoo.loaddev.components.model.Case;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CaseRepositoryTest {

    @Autowired
    private CaseRepository caseRepository;

    @BeforeEach
    void setup() {
        caseRepository.deleteAll().block();
    }

    private Case createTestCase(String ownerId) {
        return new Case(
                null,
                ownerId,
                "Lapua",
                "6.5 Creedmoor",
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("89.99"),
                "CAD",
                100);
    }

    @Test
    void saveCase() {
        var userId = randomUUID().toString();
        var savedCase = caseRepository.save(createTestCase(userId));

        create(savedCase)
                .assertNext(c -> {
                    assertThat(c.id()).isNotNull();
                    assertThat(c.ownerId()).isEqualTo(userId);
                    assertThat(c.manufacturer()).isEqualTo("Lapua");
                    assertThat(c.caliber()).isEqualTo("6.5 Creedmoor");
                    assertThat(c.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(c.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(c.currency()).isEqualTo("CAD");
                    assertThat(c.quantityPerBox()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void findCaseById() {
        var userId = randomUUID().toString();
        var savedCase = caseRepository.save(createTestCase(userId)).block();
        var foundCase = caseRepository.findByIdAndOwnerId(savedCase.id(), userId);

        create(foundCase)
                .assertNext(c -> {
                    assertThat(c.id()).isEqualTo(savedCase.id());
                    assertThat(c.ownerId()).isEqualTo(userId);
                    assertThat(c.manufacturer()).isEqualTo("Lapua");
                    assertThat(c.caliber()).isEqualTo("6.5 Creedmoor");
                    assertThat(c.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(c.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(c.currency()).isEqualTo("CAD");
                    assertThat(c.quantityPerBox()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void updateCase() {
        var userId = randomUUID().toString();
        var savedCase = caseRepository.save(createTestCase(userId)).block();

        var updatedCase = new Case(
                savedCase.id(),
                userId,
                "Peterson",
                "308 Winchester",
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("99.99"),
                "CAD",
                50);

        var result = caseRepository.save(updatedCase);

        create(result)
                .assertNext(c -> {
                    assertThat(c.id()).isEqualTo(savedCase.id());
                    assertThat(c.ownerId()).isEqualTo(userId);
                    assertThat(c.manufacturer()).isEqualTo("Peterson");
                    assertThat(c.caliber()).isEqualTo("308 Winchester");
                    assertThat(c.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(c.cost()).isEqualTo(new BigDecimal("99.99"));
                    assertThat(c.currency()).isEqualTo("CAD");
                    assertThat(c.quantityPerBox()).isEqualTo(50);
                })
                .verifyComplete();
    }

    @Test
    void deleteCase() {
        var userId = randomUUID().toString();
        var savedCase = caseRepository.save(createTestCase(userId)).block();

        var deletedCase = caseRepository.delete(savedCase);

        create(deletedCase)
                .verifyComplete();

        var foundCase = caseRepository.findByIdAndOwnerId(savedCase.id(), userId);

        create(foundCase)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var userId = randomUUID().toString();
        var case1 = createTestCase(userId);
        var case2 = new Case(
                null,
                userId,
                "Starline",
                "300 PRC",
                PrimerSize.LARGE_RIFLE_MAGNUM,
                new BigDecimal("129.99"),
                "CAD",
                50);

        caseRepository.saveAll(just(case1, case2)).blockLast();

        var result = caseRepository.findAllByOwnerId(userId);

        create(result)
                .expectNextMatches(c -> c.manufacturer().equals("Lapua"))
                .expectNextMatches(c -> c.manufacturer().equals("Starline"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQuery() {
        var userId = randomUUID().toString();
        var case1 = createTestCase(userId);

        caseRepository.saveAll(just(case1)).blockLast();

        var result = caseRepository.searchByOwnerIdAndQuery(userId, "Lapua 6.5 Creedmoor");
        create(result)
                .expectNextMatches(cc -> cc.manufacturer().equals("Lapua"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQueryNegative() {
        var ownerId = randomUUID().toString();
        var case1 = createTestCase(ownerId);

        caseRepository.saveAll(just(case1)).blockLast();

        var result = caseRepository.searchByOwnerIdAndQuery(ownerId, "Lapua 6mm BR");
        create(result)
                .expectNextCount(0)
                .verifyComplete();
    }
}

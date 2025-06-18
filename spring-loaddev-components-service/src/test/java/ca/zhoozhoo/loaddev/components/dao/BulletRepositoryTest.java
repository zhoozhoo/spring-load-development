package ca.zhoozhoo.loaddev.components.dao;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.test.StepVerifier.create;
import static ca.zhoozhoo.loaddev.components.model.Bullet.METRIC;
import static ca.zhoozhoo.loaddev.components.model.Bullet.IMPERIAL;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.model.Bullet;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BulletRepositoryTest {

    @Autowired
    private BulletRepository bulletRepository;

    @BeforeEach
    void setup() {
        bulletRepository.deleteAll().block();
    }

    private Bullet createTestBullet(String ownerId) {
        return new Bullet(
                null,
                ownerId,
                "Hornady",
                140.0,
                "ELD-Match",
                IMPERIAL,
                new BigDecimal("45.99"),
                "CAD",
                100);
    }

    @Test
    void saveBullet() {
        var userId = randomUUID().toString();
        var savedBullet = bulletRepository.save(createTestBullet(userId));

        create(savedBullet)
                .assertNext(b -> {
                    assertThat(b.id()).isNotNull();
                    assertThat(b.ownerId()).isEqualTo(userId);
                    assertThat(b.manufacturer()).isEqualTo("Hornady");
                    assertThat(b.weight()).isEqualTo(140.0);
                    assertThat(b.type()).isEqualTo("ELD-Match");
                    assertThat(b.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(b.cost()).isEqualTo(new BigDecimal("45.99"));
                    assertThat(b.currency()).isEqualTo("CAD");
                    assertThat(b.quantityPerBox()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void findBulletById() {
        var userId = randomUUID().toString();
        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();
        var foundBullet = bulletRepository.findByIdAndOwnerId(savedBullet.id(), userId);

        create(foundBullet)
                .assertNext(b -> {
                    assertThat(b.id()).isEqualTo(savedBullet.id());
                    assertThat(b.ownerId()).isEqualTo(userId);
                    assertThat(b.manufacturer()).isEqualTo("Hornady");
                    assertThat(b.weight()).isEqualTo(140.0);
                    assertThat(b.type()).isEqualTo("ELD-Match");
                    assertThat(b.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(b.cost()).isEqualTo(new BigDecimal("45.99"));
                    assertThat(b.currency()).isEqualTo("CAD");
                    assertThat(b.quantityPerBox()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void updateBullet() {
        var userId = randomUUID().toString();
        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();

        var updatedBullet = new Bullet(
                savedBullet.id(),
                userId,
                "Sierra",
                150.0,
                "MatchKing",
                METRIC,
                new BigDecimal("49.99"),
                "CAD",
                50);

        var result = bulletRepository.save(updatedBullet);

        create(result)
                .assertNext(b -> {
                    assertThat(b.id()).isEqualTo(savedBullet.id());
                    assertThat(b.ownerId()).isEqualTo(userId);
                    assertThat(b.manufacturer()).isEqualTo("Sierra");
                    assertThat(b.weight()).isEqualTo(150.0);
                    assertThat(b.type()).isEqualTo("MatchKing");
                    assertThat(b.measurementUnits()).isEqualTo(METRIC);
                    assertThat(b.cost()).isEqualTo(new BigDecimal("49.99"));
                    assertThat(b.currency()).isEqualTo("CAD");
                    assertThat(b.quantityPerBox()).isEqualTo(50);
                })
                .verifyComplete();
    }

    @Test
    void deleteBullet() {
        var userId = randomUUID().toString();
        var savedBullet = bulletRepository
                .save(createTestBullet(userId))
                .block();

        var deletedBullet = bulletRepository.delete(savedBullet);

        create(deletedBullet)
                .verifyComplete();

        var foundBullet = bulletRepository.findByIdAndOwnerId(savedBullet.id(), userId);

        create(foundBullet)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findByIdAndOwnerId() {
        var userId = randomUUID().toString();
        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();
        var result = bulletRepository.findByIdAndOwnerId(savedBullet.id(), savedBullet.ownerId());

        create(result)
                .expectNextMatches(b -> b.id().equals(savedBullet.id()))
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var userId = randomUUID().toString();
        var bullet1 = createTestBullet(userId);
        var bullet2 = new Bullet(
                null,
                userId,
                "Berger",
                168.0,
                "Hybrid Target",
                METRIC,
                new BigDecimal("54.99"),
                "CAD",
                100);

        bulletRepository.saveAll(Flux.just(bullet1, bullet2)).blockLast();

        var result = bulletRepository.findAllByOwnerId(userId);

        create(result)
                .expectNextMatches(b -> b.manufacturer().equals("Hornady"))
                .expectNextMatches(b -> b.manufacturer().equals("Berger"))
                .verifyComplete();
    }
}

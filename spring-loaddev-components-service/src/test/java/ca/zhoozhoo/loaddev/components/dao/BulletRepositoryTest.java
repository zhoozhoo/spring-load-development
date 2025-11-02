package ca.zhoozhoo.loaddev.components.dao;

import static ca.zhoozhoo.loaddev.components.model.Bullet.IMPERIAL;
import static ca.zhoozhoo.loaddev.components.model.Bullet.METRIC;
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
import ca.zhoozhoo.loaddev.components.model.Bullet;

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

        create(bulletRepository.save(createTestBullet(userId)))
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

        create(bulletRepository.findByIdAndOwnerId(savedBullet.id(), userId))
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

        create(bulletRepository.save(updatedBullet))
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
        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();

        create(bulletRepository.delete(savedBullet)).verifyComplete();
        create(bulletRepository.findByIdAndOwnerId(savedBullet.id(), userId)).expectNextCount(0).verifyComplete();
    }

    @Test
    void findByIdAndOwnerId() {
        var userId = randomUUID().toString();
        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();

        create(bulletRepository.findByIdAndOwnerId(savedBullet.id(), savedBullet.ownerId()))
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

        bulletRepository.saveAll(just(bullet1, bullet2)).blockLast();

        create(bulletRepository.findAllByOwnerId(userId))
                .expectNextMatches(b -> b.manufacturer().equals("Hornady"))
                .expectNextMatches(b -> b.manufacturer().equals("Berger"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQuery() {
        var userId = randomUUID().toString();
        bulletRepository.saveAll(just(createTestBullet(userId))).blockLast();

        create(bulletRepository.searchByOwnerIdAndQuery(userId, "Hornady ELD 140"))
                .expectNextMatches(b -> b.manufacturer().equals("Hornady"))
                .verifyComplete();
    }

    @Test
    void searchByOwnerIdAndQueryNegative() {
        var userId = randomUUID().toString();
        bulletRepository.saveAll(just(createTestBullet(userId))).blockLast();

        create(bulletRepository.searchByOwnerIdAndQuery(userId, "Hornady ELD 168"))
                .expectNextCount(0)
                .verifyComplete();
    }
}

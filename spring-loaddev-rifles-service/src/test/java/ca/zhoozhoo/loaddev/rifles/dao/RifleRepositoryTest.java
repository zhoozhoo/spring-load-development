package ca.zhoozhoo.loaddev.rifles.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.rifles.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RifleRepositoryTest {

    @Autowired
    private RifleRepository rifleRepository;

    @Test
    void saveRifle() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository
                .save(new Rifle(null, userId, "Test Rifle", "Description", "5.56mm", 20.0,
                        "Contour", "1:7", 0.2, "Rifling"));

        StepVerifier.create(savedRifle)
                .assertNext(r -> {
                    assertThat(r.id()).isNotNull();
                    assertThat(r.ownerId()).isEqualTo(userId);
                    assertThat(r.name()).isEqualTo("Test Rifle");
                    assertThat(r.description()).isEqualTo("Description");
                    assertThat(r.caliber()).isEqualTo("5.56mm");
                    assertThat(r.barrelLength()).isEqualTo(20.0);
                    assertThat(r.barrelContour()).isEqualTo("Contour");
                    assertThat(r.twistRate()).isEqualTo("1:7");
                    assertThat(r.freeBore()).isEqualTo(0.2);
                    assertThat(r.rifling()).isEqualTo("Rifling");
                })
                .verifyComplete();
    }

    @Test
    void findRifleById() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository.save(new Rifle(null, userId, "Test Rifle", "Description", "5.56mm", 20.0,
                "Contour", "1:7", 0.2, "Rifling")).block();

        var foundRifle = rifleRepository.findById(savedRifle.id());

        StepVerifier.create(foundRifle)
                .assertNext(fr -> {
                    assertThat(fr.id()).isEqualTo(savedRifle.id());
                    assertThat(fr.ownerId()).isEqualTo(userId);
                    assertThat(fr.name()).isEqualTo("Test Rifle");
                    assertThat(fr.description()).isEqualTo("Description");
                    assertThat(fr.caliber()).isEqualTo("5.56mm");
                    assertThat(fr.barrelLength()).isEqualTo(20.0);
                    assertThat(fr.barrelContour()).isEqualTo("Contour");
                    assertThat(fr.twistRate()).isEqualTo("1:7");
                    assertThat(fr.freeBore()).isEqualTo(0.2);
                    assertThat(fr.rifling()).isEqualTo("Rifling");
                })
                .verifyComplete();
    }

    @Test
    void updateRifle() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository
                .save(new Rifle(null, userId, "Test Rifle", "Description", "5.56mm", 20.0,
                        "Contour", "1:7", 0.2, "Rifling"))
                .block();

        var updatedRifle = rifleRepository.save(new Rifle(savedRifle.id(), userId, "Updated Rifle",
                "Updated Description", "7.62mm", 24.0, "Heavy",
                "1:10", 0.3, "Polygonal"));

        StepVerifier.create(updatedRifle)
                .assertNext(r -> {
                    assertThat(r.id()).isEqualTo(savedRifle.id());
                    assertThat(r.name()).isEqualTo("Updated Rifle");
                    assertThat(r.description()).isEqualTo("Updated Description");
                    assertThat(r.caliber()).isEqualTo("7.62mm");
                    assertThat(r.barrelLength()).isEqualTo(24.0);
                    assertThat(r.barrelContour()).isEqualTo("Heavy");
                    assertThat(r.twistRate()).isEqualTo("1:10");
                    assertThat(r.freeBore()).isEqualTo(0.3);
                    assertThat(r.rifling()).isEqualTo("Polygonal");
                })
                .verifyComplete();
    }

    @Test
    void deleteRifle() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository.save(new Rifle(null, userId, "Test Rifle", "Description", "5.56mm", 20.0,
                "Contour", "1:7", 0.2, "Rifling")).block();

        var deletedRifle = rifleRepository.delete(savedRifle);

        StepVerifier.create(deletedRifle)
                .verifyComplete();

        var foundRifle = rifleRepository.findById(savedRifle.id());

        StepVerifier.create(foundRifle)
                .expectNextCount(0)
                .verifyComplete();
    }
}
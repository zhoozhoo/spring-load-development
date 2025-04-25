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
import ca.zhoozhoo.loaddev.rifles.model.Unit;
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
                .save(new Rifle(null, userId,
                        "Bergara B-14 HMR",
                        "Precision rifle with molded mini-chassis",
                        "6.5 Creedmoor",
                        24.0,
                        Unit.INCHES,
                        "Heavy #5",
                        "1:8",
                        "6 Groove",
                        0.155,
                        Unit.INCHES));

        StepVerifier.create(savedRifle)
                .assertNext(r -> {
                    assertThat(r.id()).isNotNull();
                    assertThat(r.ownerId()).isEqualTo(userId);
                    assertThat(r.name()).isEqualTo("Bergara B-14 HMR");
                    assertThat(r.description()).isEqualTo("Precision rifle with molded mini-chassis");
                    assertThat(r.caliber()).isEqualTo("6.5 Creedmoor");
                    assertThat(r.barrelLength()).isEqualTo(24.0);
                    assertThat(r.barrelLengthUnit()).isEqualTo(Unit.INCHES);
                    assertThat(r.barrelContour()).isEqualTo("Heavy #5");
                    assertThat(r.twistRate()).isEqualTo("1:8");
                    assertThat(r.freeBore()).isEqualTo(0.155);
                    assertThat(r.freeBoreUnit()).isEqualTo(Unit.INCHES);
                    assertThat(r.rifling()).isEqualTo("6 Groove");
                })
                .verifyComplete();
    }

    @Test
    void findRifleById() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository.save(new Rifle(null, userId,
                "Ruger Precision Rifle",
                "Gen 3 RPR with custom barrel",
                ".300 PRC",
                26.0,
                Unit.INCHES,
                "M24",
                "1:8.5",
                "5R",
                0.158,
                Unit.INCHES)).block();

        var foundRifle = rifleRepository.findById(savedRifle.id());

        StepVerifier.create(foundRifle)
                .assertNext(fr -> {
                    assertThat(fr.id()).isEqualTo(savedRifle.id());
                    assertThat(fr.ownerId()).isEqualTo(userId);
                    assertThat(fr.name()).isEqualTo("Ruger Precision Rifle");
                    assertThat(fr.description()).isEqualTo("Gen 3 RPR with custom barrel");
                    assertThat(fr.caliber()).isEqualTo(".300 PRC");
                    assertThat(fr.barrelLength()).isEqualTo(26.0);
                    assertThat(fr.barrelLengthUnit()).isEqualTo(Unit.INCHES);
                    assertThat(fr.barrelContour()).isEqualTo("M24");
                    assertThat(fr.twistRate()).isEqualTo("1:8.5");
                    assertThat(fr.freeBore()).isEqualTo(0.158);
                    assertThat(fr.freeBoreUnit()).isEqualTo(Unit.INCHES);
                    assertThat(fr.rifling()).isEqualTo("5R");
                })
                .verifyComplete();
    }

    @Test
    void updateRifle() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository
                .save(new Rifle(null, userId,
                        "Custom 700",
                        "Remington 700 action factory configuration",
                        ".308 Winchester",
                        24.0,
                        Unit.INCHES,
                        "Sendero",
                        "1:10",
                        "6 Groove",
                        0.157,
                        Unit.INCHES))
                .block();

        var updatedRifle = rifleRepository.save(new Rifle(savedRifle.id(), userId,
                "Custom 700 PRS",
                "Remington 700 with Bartlein barrel and MDT ACC chassis",
                "6mm Creedmoor",
                26.0,
                Unit.INCHES,
                "Heavy Palma",
                "1:7.5",
                "4 Groove",
                0.153,
                Unit.INCHES));

        StepVerifier.create(updatedRifle)
                .assertNext(r -> {
                    assertThat(r.id()).isEqualTo(savedRifle.id());
                    assertThat(r.name()).isEqualTo("Custom 700 PRS");
                    assertThat(r.description()).isEqualTo("Remington 700 with Bartlein barrel and MDT ACC chassis");
                    assertThat(r.caliber()).isEqualTo("6mm Creedmoor");
                    assertThat(r.barrelLength()).isEqualTo(26.0);
                    assertThat(r.barrelLengthUnit()).isEqualTo(Unit.INCHES);
                    assertThat(r.barrelContour()).isEqualTo("Heavy Palma");
                    assertThat(r.twistRate()).isEqualTo("1:7.5");
                    assertThat(r.freeBore()).isEqualTo(0.153);
                    assertThat(r.freeBoreUnit()).isEqualTo(Unit.INCHES);
                    assertThat(r.rifling()).isEqualTo("4 Groove");
                })
                .verifyComplete();
    }

    @Test
    void deleteRifle() {
        var userId = UUID.randomUUID().toString();
        var savedRifle = rifleRepository.save(new Rifle(null, userId,
                "Savage 110 Elite Precision",
                "Chassis rifle with adjustable stock",
                "6.5 PRC",
                26.0,
                Unit.INCHES,
                "Heavy",
                "1:8",
                "5R",
                0.156,
                Unit.INCHES)).block();

        var deletedRifle = rifleRepository.delete(savedRifle);

        StepVerifier.create(deletedRifle)
                .verifyComplete();

        var foundRifle = rifleRepository.findById(savedRifle.id());

        StepVerifier.create(foundRifle)
                .expectNextCount(0)
                .verifyComplete();
    }
}
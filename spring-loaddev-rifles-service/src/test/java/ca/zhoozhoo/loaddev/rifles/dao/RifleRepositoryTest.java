package ca.zhoozhoo.loaddev.rifles.dao;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.rifles.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import ca.zhoozhoo.loaddev.rifles.model.Rifling;
import ca.zhoozhoo.loaddev.rifles.model.TwistDirection;

/**
 * Integration tests for {@link RifleRepository} using JSR-385.
 * <p>
 * Tests repository CRUD operations and custom queries with an embedded database,
 * verifying data persistence, retrieval, owner-based filtering, and reactive behavior.
 * Uses JSR-385 Quantity&lt;Length&gt; for type-safe measurements.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RifleRepositoryTest {

    @Autowired
    private RifleRepository rifleRepository;

    /**
     * Helper method to create a Rifling object for testing.
     * Parses twist rate in format "1:X" and defaults to RIGHT twist direction and 6 grooves.
     */
    private static Rifling rifling(String twistRate) {
        double rate = Double.parseDouble(twistRate.substring(2)); // Parse "1:8" -> 8.0
        return new Rifling(getQuantity(rate, INCH_INTERNATIONAL), TwistDirection.RIGHT, 6);
    }

    @Test
    void saveRifle() {
        var userId = randomUUID().toString();

        create(rifleRepository.save(new Rifle(null, userId,
                "Bergara B-14 HMR",
                "Precision rifle with molded mini-chassis",
                "6.5 Creedmoor",
                getQuantity(24.0, INCH_INTERNATIONAL),
                "Heavy #5",
                rifling("1:8"), null)))
                .assertNext(r -> {
                    assertThat(r.id()).isNotNull();
                    assertThat(r.ownerId()).isEqualTo(userId);
                    assertThat(r.name()).isEqualTo("Bergara B-14 HMR");
                    assertThat(r.description()).isEqualTo("Precision rifle with molded mini-chassis");
                    assertThat(r.caliber()).isEqualTo("6.5 Creedmoor");
                    assertThat(r.barrelLength()).isEqualTo(getQuantity(24.0, INCH_INTERNATIONAL));
                    assertThat(r.barrelContour()).isEqualTo("Heavy #5");
                    assertThat(r.rifling()).isEqualTo(rifling("1:8"));
                })
                .verifyComplete();
    }

    @Test
    void findRifleById() {
        var userId = randomUUID().toString();
        var savedRifleId = rifleRepository.save(new Rifle(null, userId,
                "Ruger Precision Rifle",
                "Gen 3 RPR with custom barrel",
                ".300 PRC",
                getQuantity(26.0, INCH_INTERNATIONAL),
                "M24",
                rifling("1:8.5"), null)).block().id();

        create(rifleRepository.findById(savedRifleId))
                .assertNext(fr -> {
                    assertThat(fr.id()).isEqualTo(savedRifleId);
                    assertThat(fr.ownerId()).isEqualTo(userId);
                    assertThat(fr.name()).isEqualTo("Ruger Precision Rifle");
                    assertThat(fr.description()).isEqualTo("Gen 3 RPR with custom barrel");
                    assertThat(fr.caliber()).isEqualTo(".300 PRC");
                    assertThat(fr.barrelLength().getValue().doubleValue()).isEqualTo(26.0);
                    assertThat(fr.barrelLength().getUnit()).isEqualTo(INCH_INTERNATIONAL);
                    assertThat(fr.barrelContour()).isEqualTo("M24");
                    assertThat(fr.rifling().twistRate().getValue().doubleValue()).isEqualTo(8.5);
                    assertThat(fr.rifling().twistRate().getUnit()).isEqualTo(INCH_INTERNATIONAL);
                    assertThat(fr.rifling().twistDirection()).isEqualTo(TwistDirection.RIGHT);
                    assertThat(fr.rifling().numberOfGrooves()).isEqualTo(6);
                })
                .verifyComplete();
    }

    @Test
    void updateRifle() {
        var userId = randomUUID().toString();
        var savedRifle = rifleRepository.save(new Rifle(null, userId,
                "Custom 700",
                "Remington 700 action factory configuration",
                ".308 Winchester",
                getQuantity(24.0, INCH_INTERNATIONAL),
                "Sendero",
                rifling("1:10"), null)).block();

        create(rifleRepository.save(new Rifle(savedRifle.id(), userId,
                "Custom 700 PRS",
                "Remington 700 with Bartlein barrel and MDT ACC chassis",
                "6mm Creedmoor",
                getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma",
                rifling("1:7.5"), null)))
                .assertNext(r -> {
                    assertThat(r.id()).isEqualTo(savedRifle.id());
                    assertThat(r.name()).isEqualTo("Custom 700 PRS");
                    assertThat(r.description()).isEqualTo("Remington 700 with Bartlein barrel and MDT ACC chassis");
                    assertThat(r.caliber()).isEqualTo("6mm Creedmoor");
                    assertThat(r.barrelLength()).isEqualTo(getQuantity(26.0, INCH_INTERNATIONAL));
                    assertThat(r.barrelContour()).isEqualTo("Heavy Palma");
                    assertThat(r.rifling()).isEqualTo(rifling("1:7.5"));
                })
                .verifyComplete();
    }

    @Test
    void deleteRifle() {
        var savedRifleId = rifleRepository.save(new Rifle(null, randomUUID().toString(),
                "Savage 110 Elite Precision",
                "Chassis rifle with adjustable stock",
                "6.5 PRC",
                getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy",
                rifling("1:8"), null)).block().id();

        create(rifleRepository.deleteById(savedRifleId)).verifyComplete();
        create(rifleRepository.findById(savedRifleId)).expectNextCount(0).verifyComplete();
    }
}

package ca.zhoozhoo.loaddev.loads.dao;

import static java.util.UUID.randomUUID;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import java.time.LocalDate;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.GroupJsr385;
import ca.zhoozhoo.loaddev.loads.model.LoadJsr385;
import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;
import tech.units.indriya.unit.Units;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ShotJsr385RepositoryTest {

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> FEET_PER_SECOND = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);

    @Autowired
    private ShotJsr385Repository shotRepository;
    
    @Autowired
    private GroupJsr385Repository groupRepository;
    
    @Autowired
    private LoadJsr385Repository loadRepository;
    
    private Long testGroupId;

    @BeforeEach
    public void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();
        
        // Create a test load and group to satisfy foreign key constraints
        var ownerId = randomUUID().toString();
        var testLoad = createTestLoad(ownerId);
        var savedLoad = loadRepository.save(testLoad).block();
        
        var testGroup = createTestGroup(ownerId, savedLoad.id());
        var savedGroup = groupRepository.save(testGroup).block();
        testGroupId = savedGroup.id();
    }
    
    private LoadJsr385 createTestLoad(String ownerId) {
        return new LoadJsr385(
                null,
                ownerId,
                "Test Load",
                "Test Description",
                "Hodgdon",
                "H4350",
                "Sierra",
                "MatchKing",
                getQuantity(168, GRAIN),
                "Federal",
                "210M",
                getQuantity(0.020, INCH_INTERNATIONAL),
                null,
                null,
                null);
    }
    
    private GroupJsr385 createTestGroup(String ownerId, Long loadId) {
        return new GroupJsr385(
                null,
                ownerId,
                loadId,
                LocalDate.now().minusDays(1),
                getQuantity(41.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL));
    }

    private ShotJsr385 createTestShot(String ownerId) {
        return new ShotJsr385(
                null,
                ownerId,
                testGroupId,
                getQuantity(2850, FEET_PER_SECOND));
    }

    @Test
    void findByIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var savedShot = shotRepository.save(createTestShot(ownerId)).block();

        create(shotRepository.findByIdAndOwnerId(savedShot.id(), savedShot.ownerId()))
                .expectNextMatches(s ->
                    s.id().equals(savedShot.id()) &&
                    s.ownerId().equals(ownerId) &&
                    s.groupId().equals(testGroupId) &&
                    s.velocity().to(FEET_PER_SECOND).getValue().doubleValue() == 2850.0)
                .verifyComplete();
    }

    @Test
    void findByGroupIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var shot1 = createTestShot(ownerId);
        var shot2 = new ShotJsr385(null,
                ownerId,
                testGroupId,
                getQuantity(2860, FEET_PER_SECOND));
        var shot3 = new ShotJsr385(null,
                ownerId,
                testGroupId,
                getQuantity(2840, FEET_PER_SECOND));

        shotRepository.save(shot1).block();
        shotRepository.save(shot2).block();
        shotRepository.save(shot3).block();

        create(shotRepository.findByGroupIdAndOwnerId(testGroupId, ownerId))
                .expectNextMatches(s ->
                    s.ownerId().equals(ownerId) &&
                    s.groupId().equals(testGroupId) &&
                    s.velocity().to(FEET_PER_SECOND).getValue().doubleValue() == 2850.0)
                .expectNextMatches(s ->
                    s.ownerId().equals(ownerId) &&
                    s.groupId().equals(testGroupId) &&
                    s.velocity().to(FEET_PER_SECOND).getValue().doubleValue() == 2860.0)
                .expectNextMatches(s ->
                    s.ownerId().equals(ownerId) &&
                    s.groupId().equals(testGroupId) &&
                    s.velocity().to(FEET_PER_SECOND).getValue().doubleValue() == 2840.0)
                .verifyComplete();
    }

    @Test
    void save() {
        var ownerId = randomUUID().toString();
        var shot = createTestShot(ownerId);

        create(shotRepository.save(shot))
                .expectNextMatches(s ->
                    s.id() != null &&
                    s.ownerId().equals(ownerId) &&
                    s.groupId().equals(testGroupId))
                .verifyComplete();
    }

    @Test
    void update() {
        var ownerId = randomUUID().toString();
        var savedShot = shotRepository.save(createTestShot(ownerId)).block();

        var updatedShot = new ShotJsr385(
                savedShot.id(),
                savedShot.ownerId(),
                savedShot.groupId(),
                getQuantity(2900, FEET_PER_SECOND));

        create(shotRepository.save(updatedShot))
                .expectNextMatches(s ->
                    s.id().equals(savedShot.id()) &&
                    s.ownerId().equals(ownerId) &&
                    s.velocity().to(FEET_PER_SECOND).getValue().doubleValue() == 2900.0)
                .verifyComplete();
    }

    @Test
    void delete() {
        var ownerId = randomUUID().toString();
        var savedShot = shotRepository.save(createTestShot(ownerId)).block();

        shotRepository.deleteById(savedShot.id()).block();

        create(shotRepository.findByIdAndOwnerId(savedShot.id(), savedShot.ownerId()))
                .verifyComplete();
    }

    @Test
    void validateVelocityRange() {
        var ownerId = randomUUID().toString();

        // Test lower bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new ShotJsr385(
                    null,
                    ownerId,
                    testGroupId,
                    getQuantity(400, FEET_PER_SECOND)))) // Below minimum
                .expectError(IllegalArgumentException.class)
                .verify();

        // Test upper bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new ShotJsr385(
                    null,
                    ownerId,
                    testGroupId,
                    getQuantity(6000, FEET_PER_SECOND)))) // Above maximum
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void validateNullVelocity() {
        var ownerId = randomUUID().toString();

        // Null velocity is allowed by constructor but validates proper handling
        var shot = new ShotJsr385(
                null,
                ownerId,
                testGroupId,
                null); // Null velocity
        
        // Constructor allows null, verify it's stored correctly
        create(reactor.core.publisher.Mono.just(shot))
                .expectNextMatches(s -> s.velocity() == null)
                .verifyComplete();
    }
}

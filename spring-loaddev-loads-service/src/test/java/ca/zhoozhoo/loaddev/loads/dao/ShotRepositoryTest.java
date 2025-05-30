package ca.zhoozhoo.loaddev.loads.dao;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;

import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ShotRepositoryTest {

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ShotRepository shotRepository;

    private Load testLoad;

    private Random random = new Random();

    @BeforeEach
    void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();

        testLoad = loadRepository.save(new Load(
                null,
                randomUUID().toString(),
                "Test Load",
                "Test Description",
                IMPERIAL,
                "Hodgdon",
                "H335",
                "Hornady",
                "FMJ",
                55.0,
                "CCI",
                "Small Rifle",
                0.02,
                2.260,
                0.002,
                1L)).block();
    }

    private Shot createTestShot(String ownerId, Long groupId) {
        return new Shot(null,
                ownerId,
                groupId,
                random.nextInt(1000));
    }

    private Group createTestGroup(String ownerId) {
        return new Group(null,
                ownerId,
                testLoad.id(),
                now(),
                26.5,
                100,
                0.40);
    }

    @Test
    void findById() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        var savedShot = shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id())).block();
        var result = shotRepository.findById(savedShot.id());

        StepVerifier.create(result)
                .expectNextMatches(s -> s.id().equals(savedShot.id()))
                .verifyComplete();
    }

    @Test
    void save() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        var savedShot = shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id()));

        StepVerifier.create(savedShot)
                .expectNextMatches(s -> s.id() != null && s.groupId().equals(savedGroup.id()))
                .verifyComplete();
    }

    @Test
    void findAll() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();

        var shot1 = createTestShot(savedGroup.ownerId(), savedGroup.id());
        var shot2 = createTestShot(savedGroup.ownerId(), savedGroup.id());

        shotRepository.saveAll(Flux.just(shot1, shot2)).blockLast();

        var result = shotRepository.findAll();

        StepVerifier.create(result)
                .expectNextMatches(s -> s.groupId().equals(shot1.groupId()))
                .expectNextMatches(s -> s.groupId().equals(shot2.groupId()))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        var savedGroup = groupRepository.save(createTestGroup(UUID.randomUUID().toString())).block();
        var savedShot = shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id())).block();

        var result = shotRepository.deleteById(savedShot.id());

        StepVerifier.create(result).verifyComplete();

        var deletedShot = shotRepository.findById(savedShot.id());

        StepVerifier.create(deletedShot)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void update() {
        var savedGroup = groupRepository.save(createTestGroup(UUID.randomUUID().toString())).block();
        var savedShot = shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id())).block();

        var updatedShot = new Shot(savedShot.id(), savedGroup.ownerId(), savedGroup.id(), random.nextInt(1000));

        var result = shotRepository.save(updatedShot);

        StepVerifier.create(result)
                .expectNextMatches(s -> s.id().equals(savedShot.id()) && s.velocity().equals(updatedShot.velocity()))
                .verifyComplete();
    }

    @Test
    void findByGroupId() {
        var savedGroup = groupRepository.save(createTestGroup(UUID.randomUUID().toString())).block();

        var shot1 = createTestShot(savedGroup.ownerId(), savedGroup.id());
        var shot2 = createTestShot(savedGroup.ownerId(), savedGroup.id());

        shotRepository.saveAll(Flux.just(shot1, shot2)).blockLast();

        var result = shotRepository.findByGroupIdAndOwnerId(savedGroup.id(), savedGroup.ownerId());

        StepVerifier.create(result)
                .expectNextMatches(s -> s.groupId().equals(shot1.groupId()))
                .expectNextMatches(s -> s.groupId().equals(shot2.groupId()))
                .verifyComplete();
    }

    @Test
    void findByLoadId() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        var shot1 = createTestShot(savedGroup.ownerId(), savedGroup.id());
        var shot2 = createTestShot(savedGroup.ownerId(), savedGroup.id());

        shotRepository.saveAll(Flux.just(shot1, shot2)).blockLast();

        var result = shotRepository.findByGroupIdAndOwnerId(savedGroup.id(), savedGroup.ownerId());

        StepVerifier.create(result)
                .expectNextMatches(s -> s.groupId().equals(shot1.groupId()))
                .expectNextMatches(s -> s.groupId().equals(shot2.groupId()))
                .verifyComplete();
    }
}

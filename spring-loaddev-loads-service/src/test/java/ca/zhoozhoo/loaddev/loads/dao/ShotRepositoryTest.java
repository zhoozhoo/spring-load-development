package ca.zhoozhoo.loaddev.loads.dao;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static reactor.test.StepVerifier.create;

import java.util.Random;

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
                2500 + random.nextInt(1000)); // 2500-3500 fps
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

        create(shotRepository.findById(savedShot.id()))
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .verifyComplete();
    }

    @Test
    void save() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();

        create(shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id())))
                .expectNextMatches(s -> s.id() != null && s.groupId().equals(savedGroup.id()))
                .verifyComplete();
    }

    @Test
    void findAll() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        shotRepository.saveAll(Flux.just(
                createTestShot(savedGroup.ownerId(), savedGroup.id()),
                createTestShot(savedGroup.ownerId(), savedGroup.id())))
                .blockLast();

        create(shotRepository.findAll())
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        var savedShot = shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id())).block();

        create(shotRepository.deleteById(savedShot.id())).verifyComplete();

        create(shotRepository.findById(savedShot.id()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void update() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        var savedShot = shotRepository.save(createTestShot(savedGroup.ownerId(), savedGroup.id())).block();

        create(shotRepository.save(new Shot(savedShot.id(), savedGroup.ownerId(), savedGroup.id(), 2500 + random.nextInt(1000))))
                .expectNextMatches(s -> s.id().equals(savedShot.id()) && s.velocity() >= 2500 && s.velocity() < 3500)
                .verifyComplete();
    }

    @Test
    void findByGroupId() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        shotRepository.saveAll(Flux.just(
                createTestShot(savedGroup.ownerId(), savedGroup.id()),
                createTestShot(savedGroup.ownerId(), savedGroup.id())))
                .blockLast();

        create(shotRepository.findByGroupIdAndOwnerId(savedGroup.id(), savedGroup.ownerId()))
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .verifyComplete();
    }

    @Test
    void findByLoadId() {
        var savedGroup = groupRepository.save(createTestGroup(randomUUID().toString())).block();
        shotRepository.saveAll(Flux.just(
                createTestShot(savedGroup.ownerId(), savedGroup.id()),
                createTestShot(savedGroup.ownerId(), savedGroup.id())))
                .blockLast();

        create(shotRepository.findByGroupIdAndOwnerId(savedGroup.id(), savedGroup.ownerId()))
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .expectNextMatches(s -> s.groupId().equals(savedGroup.id()))
                .verifyComplete();
    }
}

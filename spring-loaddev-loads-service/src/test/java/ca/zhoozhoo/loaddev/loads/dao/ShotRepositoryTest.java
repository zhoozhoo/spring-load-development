package ca.zhoozhoo.loaddev.loads.dao;

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
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ShotRepositoryTest {

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupRepository groupRepository;

    private Random random = new Random();

    @BeforeEach
    void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
    }

    @Test
    void findById() {
        var group = new Group(null, randomUUID().toString(), 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        var savedGroup = groupRepository.save(group).block();

        var shot = new Shot(null, group.ownerId(), savedGroup.id(), random.nextInt(1000));
        var savedShot = shotRepository.save(shot).block();

        var result = shotRepository.findById(savedShot.id());

        StepVerifier.create(result)
                .expectNextMatches(s -> s.id().equals(savedShot.id()))
                .verifyComplete();
    }

    @Test
    void save() {
        var group = new Group(null, randomUUID().toString(), 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        var savedGroup = groupRepository.save(group).block();

        var shot = new Shot(null, group.ownerId(), savedGroup.id(), random.nextInt(1000));

        var savedShot = shotRepository.save(shot);

        StepVerifier.create(savedShot)
                .expectNextMatches(s -> s.id() != null && s.groupId().equals(shot.groupId()))
                .verifyComplete();
    }

    @Test
    void findAll() {
        var group = new Group(null, randomUUID().toString(), 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        var savedGroup = groupRepository.save(group).block();

        var shot1 = new Shot(null, group.ownerId(), savedGroup.id(), random.nextInt(1000));
        var shot2 = new Shot(null, group.ownerId(), savedGroup.id(), random.nextInt(1000));

        shotRepository.saveAll(Flux.just(shot1, shot2)).blockLast();

        var result = shotRepository.findAll();

        StepVerifier.create(result)
                .expectNextMatches(s -> s.groupId().equals(shot1.groupId()))
                .expectNextMatches(s -> s.groupId().equals(shot2.groupId()))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        var userId = UUID.randomUUID().toString();
        var group = new Group(null, userId, 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        var savedGroup = groupRepository.save(group).block();

        var shot = new Shot(null, userId, savedGroup.id(), random.nextInt(1000));
        var savedShot = shotRepository.save(shot).block();

        var result = shotRepository.deleteById(savedShot.id());

        StepVerifier.create(result)
                .verifyComplete();

        var deletedShot = shotRepository.findById(savedShot.id());

        StepVerifier.create(deletedShot)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void update() {
        var userId = UUID.randomUUID().toString();
        var group = new Group(null, userId, 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        var savedGroup = groupRepository.save(group).block();

        var shot = new Shot(null, userId, savedGroup.id(), random.nextInt(1000));
        var savedShot = shotRepository.save(shot).block();

        var updatedShot = new Shot(savedShot.id(), userId, savedGroup.id(), random.nextInt(1000));

        var result = shotRepository.save(updatedShot);

        StepVerifier.create(result)
                .expectNextMatches(s -> s.id().equals(savedShot.id()) && s.velocity().equals(updatedShot.velocity()))
                .verifyComplete();
    }

    @Test
    void findByGroupId() {
        var userId = UUID.randomUUID().toString();
        var group = new Group(null, userId, 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        var savedGroup = groupRepository.save(group).block();

        var shot1 = new Shot(null, userId, savedGroup.id(), random.nextInt(1000));
        var shot2 = new Shot(null, userId, savedGroup.id(), random.nextInt(1000));

        shotRepository.saveAll(Flux.just(shot1, shot2)).blockLast();

        var result = shotRepository.findByGroupIdAndOwnerId(savedGroup.id(), userId);

        StepVerifier.create(result)
                .expectNextMatches(s -> s.groupId().equals(shot1.groupId()))
                .expectNextMatches(s -> s.groupId().equals(shot2.groupId()))
                .verifyComplete();
    }
}

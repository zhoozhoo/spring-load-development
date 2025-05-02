package ca.zhoozhoo.loaddev.loads.dao;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class GroupRepositoryTest {

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private LoadRepository loadRepository;

    private Load testLoad;

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

    private Group createTestGroup() {
        return createTestGroup(testLoad.ownerId(), testLoad.id());
    }

    private Group createTestGroup(String ownerId, Long loadId) {
        return new Group(null, ownerId, loadId, now(), 26.5, 100, 0.40);
    }

    @Test
    void saveGroup() {
        var group = createTestGroup();
        var savedGroup = groupRepository.save(group);

        StepVerifier.create(savedGroup)
                .assertNext(g -> {
                    assertNotNull(g.id());
                    assertEquals(testLoad.id(), g.loadId());
                    assertEquals(26.5, g.powderCharge());
                    assertEquals(100, g.targetRange());
                    assertEquals(0.40, g.groupSize());
                })
                .verifyComplete();
    }

    @Test
    void findGroupById() {
        var group = createTestGroup();
        var savedGroup = groupRepository.save(group).block();
        var foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .assertNext(fg -> {
                    assertEquals(savedGroup.id(), fg.id());
                    assertEquals(savedGroup.loadId(), fg.loadId());
                    assertEquals(26.5, fg.powderCharge());
                    assertEquals(100, fg.targetRange());
                    assertEquals(0.40, fg.groupSize());
                })
                .verifyComplete();
    }

    @Test
    void updateGroup() {
        var group = createTestGroup();
        var savedGroup = groupRepository.save(group).block();

        var updatedGroup = new Group(savedGroup.id(),
                savedGroup.ownerId(),
                savedGroup.loadId(),
                now(),
                28.0,
                200,
                0.50);

        var updatedGroupResult = groupRepository.save(updatedGroup).block();

        assertEquals(savedGroup.loadId(), updatedGroupResult.loadId());
        assertEquals(28.0, updatedGroupResult.powderCharge());
        assertEquals(200, updatedGroupResult.targetRange());
        assertEquals(0.50, updatedGroupResult.groupSize());
    }

    @Test
    void deleteGroup() {
        var group = createTestGroup();
        var savedGroup = groupRepository.save(group).block();

        groupRepository.delete(savedGroup).block();

        var foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllByLoadIdAndOwnerId() {
        var group1 = createTestGroup();
        var group2 = createTestGroup();
        groupRepository.saveAll(Flux.just(group1, group2)).blockLast();

        var result = groupRepository.findAllByLoadIdAndOwnerId(testLoad.id(), testLoad.ownerId());

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }
}
package ca.zhoozhoo.loaddev.loads.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Group;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void saveGroup() {
        var group = new Group(null, 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);
        Mono<Group> savedGroup = groupRepository.save(group);

        StepVerifier.create(savedGroup)
                .assertNext(g -> {
                    assertNotNull(g.id());
                    assertEquals(15, g.numberOfShots());
                    assertEquals(100, g.targetRange());
                    assertEquals(0.40, g.groupSize());
                    assertEquals(2874, g.mean());
                    assertEquals(2732, g.median());
                    assertEquals(2721, g.min());
                    assertEquals(2995, g.max());
                    assertEquals(27, g.standardDeviation());
                    assertEquals(54, g.extremeSpread());
                })
                .verifyComplete();
    }

    @Test
    void findGroupById() {
        var group = new Group(null, 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);

        var savedGroup = groupRepository.save(group).block();

        Mono<Group> foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .assertNext(fg -> {
                    assertEquals(savedGroup.id(), fg.id());
                    assertEquals(15, fg.numberOfShots());
                    assertEquals(100, fg.targetRange());
                    assertEquals(0.40, fg.groupSize());
                    assertEquals(2874, fg.mean());
                    assertEquals(2732, fg.median());
                    assertEquals(2721, fg.min());
                    assertEquals(2995, fg.max());
                    assertEquals(27, fg.standardDeviation());
                    assertEquals(54, fg.extremeSpread());
                })
                .verifyComplete();
    }

    @Test
    void updateGroup() {
        var group = new Group(null, 15, 100, 0.40, 2874, 2732, 2721, 2995, 27, 54);

        var savedGroup = groupRepository.save(group).block();

        var updatedGroup = new Group(savedGroup.id(), 20, 200, 0.50, 3000, 2800, 2750, 3050, 30, 60);
        var updatedGroupResult = groupRepository.save(updatedGroup).block();

        assertEquals(20, updatedGroupResult.numberOfShots());
        assertEquals(200, updatedGroupResult.targetRange());
        assertEquals(0.50, updatedGroupResult.groupSize());
        assertEquals(3000, updatedGroupResult.mean());
        assertEquals(2800, updatedGroupResult.median());
        assertEquals(2750, updatedGroupResult.min());
        assertEquals(3050, updatedGroupResult.max());
        assertEquals(30, updatedGroupResult.standardDeviation());
        assertEquals(60, updatedGroupResult.extremeSpread());
    }

    @Test
    void deleteGroup() {
        var group = new Group(null, 10, 100, 1.5, 5, 4, 1, 9, 2, 8);

        var savedGroup = groupRepository.save(group).block();

        groupRepository.delete(savedGroup).block();

        Mono<Group> foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .expectNextCount(0)
                .verifyComplete();
    }
}
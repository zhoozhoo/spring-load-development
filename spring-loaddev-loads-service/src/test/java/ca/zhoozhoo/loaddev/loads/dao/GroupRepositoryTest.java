package ca.zhoozhoo.loaddev.loads.dao;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Unit;
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
        var group = new Group(null, 
                randomUUID().toString(), 
                26.5, 
                Unit.GRAINS,
                100, 
                Unit.YARDS,
                0.40, 
                Unit.INCHES);
                
        Mono<Group> savedGroup = groupRepository.save(group);

        StepVerifier.create(savedGroup)
                .assertNext(g -> {
                    assertNotNull(g.id());
                    assertEquals(26.5, g.powderCharge());
                    assertEquals(Unit.GRAINS, g.powderChargeUnit());
                    assertEquals(100, g.targetRange());
                    assertEquals(Unit.YARDS, g.targetRangeUnit());
                    assertEquals(0.40, g.groupSize());
                    assertEquals(Unit.INCHES, g.groupSizeUnit());
                })
                .verifyComplete();
    }

    @Test
    void findGroupById() {
        var group = new Group(null, 
                randomUUID().toString(), 
                26.5, 
                Unit.GRAINS,
                100, 
                Unit.YARDS,
                0.40, 
                Unit.INCHES);

        var savedGroup = groupRepository.save(group).block();

        Mono<Group> foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .assertNext(fg -> {
                    assertEquals(savedGroup.id(), fg.id());
                    assertEquals(26.5, fg.powderCharge());
                    assertEquals(Unit.GRAINS, fg.powderChargeUnit());
                    assertEquals(100, fg.targetRange());
                    assertEquals(Unit.YARDS, fg.targetRangeUnit());
                    assertEquals(0.40, fg.groupSize());
                    assertEquals(Unit.INCHES, fg.groupSizeUnit());
                })
                .verifyComplete();
    }

    @Test
    void updateGroup() {
        var group = new Group(null, 
                randomUUID().toString(), 
                26.5, 
                Unit.GRAINS,
                100, 
                Unit.YARDS,
                0.40, 
                Unit.INCHES);

        var savedGroup = groupRepository.save(group).block();

        var updatedGroup = new Group(savedGroup.id(), 
                savedGroup.ownerId(), 
                28.0, 
                Unit.GRAINS,
                200, 
                Unit.YARDS,
                0.50, 
                Unit.INCHES);
        var updatedGroupResult = groupRepository.save(updatedGroup).block();

        assertEquals(28.0, updatedGroupResult.powderCharge());
        assertEquals(Unit.GRAINS, updatedGroupResult.powderChargeUnit());
        assertEquals(200, updatedGroupResult.targetRange());
        assertEquals(Unit.YARDS, updatedGroupResult.targetRangeUnit());
        assertEquals(0.50, updatedGroupResult.groupSize());
        assertEquals(Unit.INCHES, updatedGroupResult.groupSizeUnit());
    }

    @Test
    void deleteGroup() {
        var group = new Group(null, 
                randomUUID().toString(), 
                26.5, 
                Unit.GRAINS,
                100, 
                Unit.YARDS,
                0.40, 
                Unit.INCHES);

        var savedGroup = groupRepository.save(group).block();

        groupRepository.delete(savedGroup).block();

        Mono<Group> foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .expectNextCount(0)
                .verifyComplete();
    }
}
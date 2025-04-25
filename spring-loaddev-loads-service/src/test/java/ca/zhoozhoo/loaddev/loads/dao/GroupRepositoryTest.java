package ca.zhoozhoo.loaddev.loads.dao;

import static ca.zhoozhoo.loaddev.loads.model.Unit.GRAINS;
import static ca.zhoozhoo.loaddev.loads.model.Unit.INCHES;
import static ca.zhoozhoo.loaddev.loads.model.Unit.YARDS;
import static java.time.LocalDate.now;
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
                now(), 
                26.5, 
                GRAINS,
                100, 
                YARDS,
                0.40, 
                INCHES);
                
        var savedGroup = groupRepository.save(group);

        StepVerifier.create(savedGroup)
                .assertNext(g -> {
                    assertNotNull(g.id());
                    assertEquals(26.5, g.powderCharge());
                    assertEquals(GRAINS, g.powderChargeUnit());
                    assertEquals(100, g.targetRange());
                    assertEquals(YARDS, g.targetRangeUnit());
                    assertEquals(0.40, g.groupSize());
                    assertEquals(INCHES, g.groupSizeUnit());
                })
                .verifyComplete();
    }

    @Test
    void findGroupById() {
        var group = new Group(null, 
                randomUUID().toString(),
                now(), 
                26.5, 
                GRAINS,
                100, 
                YARDS,
                0.40, 
                INCHES);

        var savedGroup = groupRepository.save(group).block();

        var foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .assertNext(fg -> {
                    assertEquals(savedGroup.id(), fg.id());
                    assertEquals(26.5, fg.powderCharge());
                    assertEquals(GRAINS, fg.powderChargeUnit());
                    assertEquals(100, fg.targetRange());
                    assertEquals(YARDS, fg.targetRangeUnit());
                    assertEquals(0.40, fg.groupSize());
                    assertEquals(INCHES, fg.groupSizeUnit());
                })
                .verifyComplete();
    }

    @Test
    void updateGroup() {
        var group = new Group(null, 
                randomUUID().toString(),
                now(), 
                26.5, 
                GRAINS,
                100, 
                YARDS,
                0.40, 
                INCHES);

        var savedGroup = groupRepository.save(group).block();

        var updatedGroup = new Group(savedGroup.id(), 
                savedGroup.ownerId(),
                now(), 
                28.0, 
                GRAINS,
                200, 
                YARDS,
                0.50, 
                INCHES);
        var updatedGroupResult = groupRepository.save(updatedGroup).block();

        assertEquals(28.0, updatedGroupResult.powderCharge());
        assertEquals(GRAINS, updatedGroupResult.powderChargeUnit());
        assertEquals(200, updatedGroupResult.targetRange());
        assertEquals(YARDS, updatedGroupResult.targetRangeUnit());
        assertEquals(0.50, updatedGroupResult.groupSize());
        assertEquals(INCHES, updatedGroupResult.groupSizeUnit());
    }

    @Test
    void deleteGroup() {
        var group = new Group(null, 
                randomUUID().toString(),
                now(), 
                26.5, 
                GRAINS,
                100, 
                YARDS,
                0.40, 
                INCHES);

        var savedGroup = groupRepository.save(group).block();

        groupRepository.delete(savedGroup).block();

        var foundGroup = groupRepository.findById(savedGroup.id());

        StepVerifier.create(foundGroup)
                .expectNextCount(0)
                .verifyComplete();
    }
}
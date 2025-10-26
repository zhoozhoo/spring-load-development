package ca.zhoozhoo.loaddev.loads.service;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static reactor.test.StepVerifier.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;
import ca.zhoozhoo.loaddev.loads.model.Shot;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class LoadsServiceTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private LoadsService loadsService;

    private static final String USER_ID = randomUUID().toString();
    private Load testLoad;
    private Group testGroup;

    @BeforeEach
    void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();

        testLoad = loadRepository.save(new Load(
                null,
                USER_ID,
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

        testGroup = groupRepository.save(new Group(
                null,
                USER_ID,
                testLoad.id(),
                now(),
                24.0,
                100,
                1.0)).block();
    }

    private Shot createShot(int velocity) {
        return new Shot(null, USER_ID, testGroup.id(), velocity);
    }

    @Test
    void getGroupStatistics() {
        // Create shots with known velocities
        shotRepository.save(createShot(2800)).block();
        shotRepository.save(createShot(2820)).block();
        shotRepository.save(createShot(2810)).block();

       create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                .assertNext(stats -> {
                    assertNotNull(stats);
                    assertEquals(testGroup.date(), stats.date());
                    assertEquals(24.0, stats.powderCharge(), 0.0);
                    assertEquals(100, stats.targetRange());
                    assertEquals(1.0, stats.groupSize(), 0.0);
                    assertEquals(2810.0, stats.averageVelocity(), 0.01);
                    assertEquals(8.16, stats.standardDeviation(), 0.1);
                    assertEquals(20.0, stats.extremeSpread(), 0.0);
                    assertEquals(3, stats.shots().size());
                })
                .verifyComplete();
    }

    @Test
    void getGroupStatisticsWithNoShots() {
       create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                .assertNext(stats -> {
                    assertNotNull(stats);
                    assertEquals(testGroup.date(), stats.date());
                    assertEquals(0.0, stats.averageVelocity(), 0.0);
                    assertEquals(0.0, stats.standardDeviation(), 0.0);
                    assertEquals(0.0, stats.extremeSpread(), 0.0);
                    assertEquals(0, stats.shots().size());
                })
                .verifyComplete();
    }

    @Test
    void getGroupStatisticsForNonExistentGroup() {
       create(loadsService.getGroupStatistics(999L, USER_ID))
                .verifyComplete();
    }

    @Test
    void getGroupStatisticsWithSingleShot() {
        shotRepository.save(createShot(2800)).block();

       create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                .assertNext(stats -> {
                    assertNotNull(stats);
                    assertEquals(2800.0, stats.averageVelocity(), 0.0);
                    assertEquals(0.0, stats.standardDeviation(), 0.0);
                    assertEquals(0.0, stats.extremeSpread(), 0.0);
                    assertEquals(1, stats.shots().size());
                })
                .verifyComplete();
    }
}

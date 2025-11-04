package ca.zhoozhoo.loaddev.loads.service;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupJsr385Repository;
import ca.zhoozhoo.loaddev.loads.dao.LoadJsr385Repository;
import ca.zhoozhoo.loaddev.loads.dao.ShotJsr385Repository;
import ca.zhoozhoo.loaddev.loads.model.GroupJsr385;
import ca.zhoozhoo.loaddev.loads.model.LoadJsr385;
import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;

/**
 * Integration tests for {@link LoadsServiceJsr385}.
 * <p>
 * Tests the service layer integration with JSR-385 repositories,
 * verifying correct handling of Quantity types, unit conversions,
 * and reactive data flows.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("LoadsServiceJsr385 Integration Tests")
class LoadsServiceJsr385Test {

    @Autowired
    private GroupJsr385Repository groupRepository;

    @Autowired
    private ShotJsr385Repository shotRepository;

    @Autowired
    private LoadJsr385Repository loadRepository;

    @Autowired
    private LoadsServiceJsr385 loadsService;

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> FEET_PER_SECOND = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);
    
    @SuppressWarnings("unchecked")
    private static final Unit<Speed> METRES_PER_SECOND = (Unit<Speed>) METRE.divide(SECOND);

    private static final String USER_ID = randomUUID().toString();
    private LoadJsr385 testLoad;
    private GroupJsr385 testGroup;

    @BeforeEach
    void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();

        testLoad = createTestLoad("Test Load JSR-385", "Test Description");

        testGroup = groupRepository.save(new GroupJsr385(
                null,
                USER_ID,
                testLoad.id(),
                now(),
                getQuantity(24.0, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(1.0, INCH_INTERNATIONAL))).block();
    }

    private LoadJsr385 createTestLoad(String name, String description) {
        return loadRepository.save(new LoadJsr385(
                null,
                USER_ID,
                name,
                description,
                "Hodgdon",
                "H335",
                "Hornady",
                "FMJ",
                getQuantity(55.0, GRAIN),
                "CCI",
                "Small Rifle",
                getQuantity(0.02, INCH_INTERNATIONAL),
                getQuantity(2.260, INCH_INTERNATIONAL),
                getQuantity(0.002, INCH_INTERNATIONAL),
                1L)).block();
    }

    private ShotJsr385 createShot(double velocity, Unit<Speed> unit) {
        return new ShotJsr385(null, USER_ID, testGroup.id(), getQuantity(velocity, unit));
    }

    @Nested
    @DisplayName("Get Group Statistics Tests")
    class GetGroupStatisticsTests {

        @Test
        @DisplayName("Should compute statistics for group with multiple shots")
        void shouldComputeStatisticsForGroupWithMultipleShots() {
            // Given
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(2820.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(2810.0, FEET_PER_SECOND)).block();

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(testGroup.date(), stats.date());
                        assertEquals(24.0, stats.powderCharge().getValue().doubleValue(), 0.0);
                        assertEquals(GRAIN, stats.powderCharge().getUnit());
                        assertEquals(100.0, stats.targetRange().getValue().doubleValue(), 0.0);
                        assertEquals(YARD_INTERNATIONAL, stats.targetRange().getUnit());
                        assertEquals(1.0, stats.groupSize().getValue().doubleValue(), 0.0);
                        assertEquals(INCH_INTERNATIONAL, stats.groupSize().getUnit());
                        assertEquals(2810.0, stats.averageVelocity().getValue().doubleValue(), 0.01);
                        assertEquals(FEET_PER_SECOND, stats.averageVelocity().getUnit());
                        assertEquals(8.16, stats.standardDeviation().getValue().doubleValue(), 0.1);
                        assertEquals(20.0, stats.extremeSpread().getValue().doubleValue(), 0.0);
                        assertEquals(3, stats.shots().size());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle group with no shots")
        void shouldHandleGroupWithNoShots() {
            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(testGroup.date(), stats.date());
                        assertEquals(0.0, stats.averageVelocity().getValue().doubleValue());
                        assertEquals(0.0, stats.standardDeviation().getValue().doubleValue());
                        assertEquals(0.0, stats.extremeSpread().getValue().doubleValue());
                        assertEquals(0, stats.shots().size());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle group with single shot")
        void shouldHandleGroupWithSingleShot() {
            // Given
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(2800.0, stats.averageVelocity().getValue().doubleValue());
                        assertEquals(0.0, stats.standardDeviation().getValue().doubleValue());
                        assertEquals(0.0, stats.extremeSpread().getValue().doubleValue());
                        assertEquals(1, stats.shots().size());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty for non-existent group")
        void shouldReturnEmptyForNonExistentGroup() {
            // When/Then
            create(loadsService.getGroupStatistics(999L, USER_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle shots with mixed units")
        void shouldHandleShotsWithMixedUnits() {
            // Given - first shot determines unit
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(853.44, METRES_PER_SECOND)).block();  // ~2800 fps

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        // Should use fps since first shot was in fps
                        assertEquals(FEET_PER_SECOND, stats.averageVelocity().getUnit());
                        assertEquals(2800.0, stats.averageVelocity().getValue().doubleValue(), 0.1);
                        assertEquals(2, stats.shots().size());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should isolate by user ID")
        void shouldIsolateByUserId() {
            // Given
            var otherUserId = randomUUID().toString();
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            
            // Create shot for different user
            shotRepository.save(new ShotJsr385(
                    null, 
                    otherUserId, 
                    testGroup.id(), 
                    getQuantity(3000.0, FEET_PER_SECOND))).block();

            // When/Then - should only see shot for USER_ID
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(1, stats.shots().size());
                        assertEquals(2800.0, stats.averageVelocity().getValue().doubleValue(), 0.0);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Get Group Statistics For Load Tests")
    class GetGroupStatisticsForLoadTests {

        @Test
        @DisplayName("Should retrieve statistics for all groups in load")
        void shouldRetrieveStatisticsForAllGroupsInLoad() {
            // Given - create second group
            var secondGroup = groupRepository.save(new GroupJsr385(
                    null,
                    USER_ID,
                    testLoad.id(),
                    now(),
                    getQuantity(24.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.8, INCH_INTERNATIONAL))).block();

            // Add shots to both groups
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            shotRepository.save(new ShotJsr385(
                    null, 
                    USER_ID, 
                    secondGroup.id(), 
                    getQuantity(2810.0, FEET_PER_SECOND))).block();

            // When/Then
            create(loadsService.getGroupStatisticsForLoad(testLoad.id(), USER_ID))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty flux for load with no groups")
        void shouldReturnEmptyFluxForLoadWithNoGroups() {
            // Given - create load with no groups
            var emptyLoad = createTestLoad("Empty Load", "No groups");

            // When/Then
            create(loadsService.getGroupStatisticsForLoad(emptyLoad.id(), USER_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should isolate groups by user ID")
        void shouldIsolateGroupsByUserId() {
            // Given - create group for different user with same loadId
            var otherUserId = randomUUID().toString();
            
            groupRepository.save(new GroupJsr385(
                    null,
                    otherUserId,
                    testLoad.id(),
                    now(),
                    getQuantity(25.0, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(1.5, INCH_INTERNATIONAL))).block();

            // When/Then - should only see groups for USER_ID
            create(loadsService.getGroupStatisticsForLoad(testLoad.id(), USER_ID))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should compute correct statistics for each group")
        void shouldComputeCorrectStatisticsForEachGroup() {
            // Given
            var secondGroup = groupRepository.save(new GroupJsr385(
                    null,
                    USER_ID,
                    testLoad.id(),
                    now(),
                    getQuantity(24.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.8, INCH_INTERNATIONAL))).block();

            // Add different velocities to each group
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(2810.0, FEET_PER_SECOND)).block();
            
            shotRepository.save(new ShotJsr385(
                    null, 
                    USER_ID, 
                    secondGroup.id(), 
                    getQuantity(2900.0, FEET_PER_SECOND))).block();
            shotRepository.save(new ShotJsr385(
                    null, 
                    USER_ID, 
                    secondGroup.id(), 
                    getQuantity(2910.0, FEET_PER_SECOND))).block();

            // When/Then
            create(loadsService.getGroupStatisticsForLoad(testLoad.id(), USER_ID))
                    .expectNextMatches(stats -> 
                        stats.averageVelocity().getValue().doubleValue() > 2804 
                        && stats.averageVelocity().getValue().doubleValue() < 2806
                        || stats.averageVelocity().getValue().doubleValue() > 2904 
                        && stats.averageVelocity().getValue().doubleValue() < 2906)
                    .expectNextMatches(stats -> 
                        stats.averageVelocity().getValue().doubleValue() > 2804 
                        && stats.averageVelocity().getValue().doubleValue() < 2806
                        || stats.averageVelocity().getValue().doubleValue() > 2904 
                        && stats.averageVelocity().getValue().doubleValue() < 2906)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Unit Handling Tests")
    class UnitHandlingTests {

        @Test
        @DisplayName("Should preserve unit from first shot in group")
        void shouldPreserveUnitFromFirstShotInGroup() {
            // Given - first shot in meters per second
            shotRepository.save(createShot(853.44, METRES_PER_SECOND)).block();
            shotRepository.save(createShot(856.49, METRES_PER_SECOND)).block();

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(METRES_PER_SECOND, stats.averageVelocity().getUnit());
                        assertEquals(854.965, stats.averageVelocity().getValue().doubleValue(), 2.0);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should use default unit when no shots present")
        void shouldUseDefaultUnitWhenNoShotsPresent() {
            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        // Should use DEFAULT_VELOCITY_UNIT (feet per second)
                        assertEquals(FEET_PER_SECOND, stats.averageVelocity().getUnit());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should correctly convert mixed units to first shot unit")
        void shouldCorrectlyConvertMixedUnitsToFirstShotUnit() {
            // Given - first in fps, second in mps
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(856.49, METRES_PER_SECOND)).block();  // ~2810 fps

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(FEET_PER_SECOND, stats.averageVelocity().getUnit());
                        // Average should be ~2805 fps
                        assertEquals(2805.0, stats.averageVelocity().getValue().doubleValue(), 0.5);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle group with very similar velocities")
        void shouldHandleGroupWithVerySimilarVelocities() {
            // Given
            shotRepository.save(createShot(2800.1, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(2800.2, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(2800.3, FEET_PER_SECOND)).block();

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(2800.2, stats.averageVelocity().getValue().doubleValue(), 0.05);
                        assertEquals(0.2, stats.extremeSpread().getValue().doubleValue(), 0.05);
                        // Standard deviation should be very small but positive
                        assertEquals(0.0816, stats.standardDeviation().getValue().doubleValue(), 0.01);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle large number of shots")
        void shouldHandleLargeNumberOfShots() {
            // Given - 50 shots around 2800 fps
            for (int i = 0; i < 50; i++) {
                shotRepository.save(createShot(2800.0 + (i % 10), FEET_PER_SECOND)).block();
            }

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(50, stats.shots().size());
                        assertEquals(2804.5, stats.averageVelocity().getValue().doubleValue(), 0.1);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle extreme velocity outliers")
        void shouldHandleExtremeVelocityOutliers() {
            // Given - one outlier
            shotRepository.save(createShot(2800.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(2810.0, FEET_PER_SECOND)).block();
            shotRepository.save(createShot(3000.0, FEET_PER_SECOND)).block();  // outlier

            // When/Then
            create(loadsService.getGroupStatistics(testGroup.id(), USER_ID))
                    .assertNext(stats -> {
                        assertEquals(3, stats.shots().size());
                        assertEquals(200.0, stats.extremeSpread().getValue().doubleValue(), 0.0);
                        // Standard deviation should be large
                        assertEquals(92.01449161228342, stats.standardDeviation().getValue().doubleValue(), 2.5);
                    })
                    .verifyComplete();
        }
    }
}

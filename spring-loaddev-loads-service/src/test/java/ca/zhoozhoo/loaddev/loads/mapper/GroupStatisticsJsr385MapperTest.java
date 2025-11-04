package ca.zhoozhoo.loaddev.loads.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mapstruct.factory.Mappers.getMapper;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.SECOND;

import java.time.LocalDate;
import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.zhoozhoo.loaddev.loads.model.GroupJsr385;
import ca.zhoozhoo.loaddev.loads.model.GroupStatisticsJsr385;
import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;

/**
 * Unit tests for the {@link GroupStatisticsJsr385Mapper}.
 * <p>
 * Tests the mapping functionality between JSR-385 domain models and DTOs,
 * including Quantity object handling and nested object mapping.
 * </p>
 *
 * @author Zhubin Salehi
 */
@DisplayName("GroupStatisticsJsr385Mapper Tests")
class GroupStatisticsJsr385MapperTest {

    private GroupStatisticsJsr385Mapper mapper;

    // Test data constants
    private static final String OWNER_ID = "user123";
    private static final Long LOAD_ID = 1L;
    private static final Long GROUP_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 11, 1);

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> FEET_PER_SECOND = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);

    @BeforeEach
    void setUp() {
        mapper = getMapper(GroupStatisticsJsr385Mapper.class);
    }

    /**
     * Creates a valid GroupJsr385 instance for testing.
     */
    private GroupJsr385 createValidGroup() {
        return new GroupJsr385(
            GROUP_ID,
            OWNER_ID,
            LOAD_ID,
            TEST_DATE,
            getQuantity(42.5, GRAIN),
            getQuantity(100, YARD_INTERNATIONAL),
            getQuantity(0.75, INCH_INTERNATIONAL)
        );
    }

    /**
     * Creates a valid ShotJsr385 instance for testing.
     */
    private ShotJsr385 createValidShot(Long id, double velocity) {
        return new ShotJsr385(
            id,
            OWNER_ID,
            GROUP_ID,
            getQuantity(velocity, FEET_PER_SECOND)
        );
    }

    /**
     * Creates a valid GroupStatisticsJsr385 instance for testing.
     */
    private GroupStatisticsJsr385 createValidGroupStatistics() {
        var group = createValidGroup();
        var shots = List.of(
            createValidShot(1L, 2800.0),
            createValidShot(2L, 2810.0),
            createValidShot(3L, 2790.0),
            createValidShot(4L, 2805.0),
            createValidShot(5L, 2795.0)
        );

        return new GroupStatisticsJsr385(
            group,
            getQuantity(2800.0, FEET_PER_SECOND),    // average velocity
            getQuantity(7.9, FEET_PER_SECOND),        // standard deviation
            getQuantity(20.0, FEET_PER_SECOND),       // extreme spread
            shots
        );
    }

    @Nested
    @DisplayName("GroupStatistics Mapping Tests")
    class GroupStatisticsMappingTests {

        @Test
        @DisplayName("Should map GroupStatisticsJsr385 to DTO with all fields")
        void shouldMapGroupStatisticsToDtoWithAllFields() {
            // When
            var dto = mapper.toDto(createValidGroupStatistics());

            // Then
            assertNotNull(dto);
            assertEquals(TEST_DATE, dto.date());
            assertNotNull(dto.powderCharge());
            assertEquals(42.5, dto.powderCharge().getValue().doubleValue(), 0.01);
            assertNotNull(dto.targetRange());
            assertEquals(100, dto.targetRange().getValue().doubleValue(), 0.01);
            assertNotNull(dto.groupSize());
            assertEquals(0.75, dto.groupSize().getValue().doubleValue(), 0.01);
            assertNotNull(dto.averageVelocity());
            assertEquals(2800.0, dto.averageVelocity().getValue().doubleValue(), 0.01);
            assertNotNull(dto.standardDeviation());
            assertEquals(7.9, dto.standardDeviation().getValue().doubleValue(), 0.01);
            assertNotNull(dto.extremeSpread());
            assertEquals(20.0, dto.extremeSpread().getValue().doubleValue(), 0.01);
            assertNotNull(dto.shots());
            assertEquals(5, dto.shots().size());
        }

        @Test
        @DisplayName("Should preserve Quantity units when mapping")
        void shouldPreserveQuantityUnitsWhenMapping() {
            // When
            var dto = mapper.toDto(createValidGroupStatistics());

            // Then
            assertEquals(GRAIN, dto.powderCharge().getUnit());
            assertEquals(YARD_INTERNATIONAL, dto.targetRange().getUnit());
            assertEquals(INCH_INTERNATIONAL, dto.groupSize().getUnit());
            assertEquals(FEET_PER_SECOND, dto.averageVelocity().getUnit());
            assertEquals(FEET_PER_SECOND, dto.standardDeviation().getUnit());
            assertEquals(FEET_PER_SECOND, dto.extremeSpread().getUnit());
        }

        @Test
        @DisplayName("Should map shots list correctly")
        void shouldMapShotsListCorrectly() {
            // When
            var shotDtos = mapper.toDto(createValidGroupStatistics()).shots();

            // Then
            assertEquals(5, shotDtos.size());
            assertEquals(2800.0, shotDtos.get(0).velocity().getValue().doubleValue(), 0.01);
            assertEquals(2810.0, shotDtos.get(1).velocity().getValue().doubleValue(), 0.01);
            assertEquals(2790.0, shotDtos.get(2).velocity().getValue().doubleValue(), 0.01);
            assertEquals(2805.0, shotDtos.get(3).velocity().getValue().doubleValue(), 0.01);
            assertEquals(2795.0, shotDtos.get(4).velocity().getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should handle empty shots list")
        void shouldHandleEmptyShotsList() {
            // When
            var dto = mapper.toDto(new GroupStatisticsJsr385(
                createValidGroup(),
                getQuantity(0.0, FEET_PER_SECOND),
                getQuantity(0.0, FEET_PER_SECOND),
                getQuantity(0.0, FEET_PER_SECOND),
                List.of()
            ));

            // Then
            assertNotNull(dto);
            assertNotNull(dto.shots());
            assertTrue(dto.shots().isEmpty());
        }

        @Test
        @DisplayName("Should handle null GroupStatistics")
        void shouldHandleNullGroupStatistics() {
            // When
            var dto = mapper.toDto(null);

            // Then
            assertNull(dto);
        }

        @Test
        @DisplayName("Should map group statistics with different units")
        void shouldMapGroupStatisticsWithDifferentUnits() {
            // Given - Create group with metric units where applicable
            @SuppressWarnings("unchecked")
            var metresPerSecond = (Unit<Speed>) METRE.divide(SECOND);
            
            // When
            var dto = mapper.toDto(new GroupStatisticsJsr385(
                new GroupJsr385(
                    GROUP_ID,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(2.75, GRAIN),  // powder charge (still in grains)
                    getQuantity(91.44, METRE),  // 100 yards in meters
                    getQuantity(0.01905, METRE)  // 0.75 inches in meters
                ),
                getQuantity(853.44, metresPerSecond),  // ~2800 fps in m/s
                getQuantity(2.4, metresPerSecond),     // ~7.9 fps in m/s
                getQuantity(6.1, metresPerSecond),     // ~20 fps in m/s
                List.of(new ShotJsr385(1L, OWNER_ID, GROUP_ID, getQuantity(853.44, metresPerSecond)))
            ));

            // Then
            assertNotNull(dto);
            assertNotNull(dto.powderCharge());
            assertNotNull(dto.targetRange());
            assertNotNull(dto.groupSize());
            assertNotNull(dto.averageVelocity());
            // The values should be preserved with their original units
            assertEquals(METRE, dto.targetRange().getUnit());
            assertEquals(METRE, dto.groupSize().getUnit());
            assertEquals(metresPerSecond, dto.averageVelocity().getUnit());
        }
    }

    @Nested
    @DisplayName("Shot Mapping Tests")
    class ShotMappingTests {

        @Test
        @DisplayName("Should map ShotJsr385 to DTO")
        void shouldMapShotToDto() {
            // When
            var dto = mapper.shotToShotDto(createValidShot(1L, 2800.0));

            // Then
            assertNotNull(dto);
            assertNotNull(dto.velocity());
            assertEquals(2800.0, dto.velocity().getValue().doubleValue(), 0.01);
            assertEquals(FEET_PER_SECOND, dto.velocity().getUnit());
        }

        @Test
        @DisplayName("Should map shot with different velocity values")
        void shouldMapShotWithDifferentVelocityValues() {
            // Test various velocity values
            for (var velocity : new double[]{500.0, 1500.0, 2800.0, 3500.0, 4999.0}) {
                // When
                var dto = mapper.shotToShotDto(createValidShot(1L, velocity));

                // Then
                assertNotNull(dto);
                assertEquals(velocity, dto.velocity().getValue().doubleValue(), 0.01,
                    "Failed for velocity: " + velocity);
            }
        }

        @Test
        @DisplayName("Should handle null shot")
        void shouldHandleNullShot() {
            // When
            var dto = mapper.shotToShotDto(null);

            // Then
            assertNull(dto);
        }

        @Test
        @DisplayName("Should preserve velocity unit when mapping shot")
        void shouldPreserveVelocityUnitWhenMappingShot() {
            // Given
            @SuppressWarnings("unchecked")
            var metresPerSecond = (Unit<Speed>) METRE.divide(SECOND);

            // When
            var dto = mapper.shotToShotDto(new ShotJsr385(
                1L,
                OWNER_ID,
                GROUP_ID,
                getQuantity(853.44, metresPerSecond)  // ~2800 fps
            ));

            // Then
            assertNotNull(dto);
            assertEquals(metresPerSecond, dto.velocity().getUnit());
            assertEquals(853.44, dto.velocity().getValue().doubleValue(), 0.01);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle statistics with null optional fields in group")
        void shouldHandleStatisticsWithNullOptionalFieldsInGroup() {
            // When
            var dto = mapper.toDto(new GroupStatisticsJsr385(
                new GroupJsr385(
                    GROUP_ID,
                    OWNER_ID,
                    LOAD_ID,
                    TEST_DATE,
                    getQuantity(42.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    null  // null group size
                ),
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(7.9, FEET_PER_SECOND),
                getQuantity(20.0, FEET_PER_SECOND),
                List.of()
            ));

            // Then
            assertNotNull(dto);
            assertNull(dto.groupSize());
            assertNotNull(dto.powderCharge());
            assertNotNull(dto.targetRange());
        }

        @Test
        @DisplayName("Should handle very small velocity values")
        void shouldHandleVerySmallVelocityValues() {
            // When
            var dto = mapper.shotToShotDto(createValidShot(1L, 500.0));  // minimum valid velocity

            // Then
            assertNotNull(dto);
            assertEquals(500.0, dto.velocity().getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should handle very large velocity values")
        void shouldHandleVeryLargeVelocityValues() {
            // When
            var dto = mapper.shotToShotDto(createValidShot(1L, 5000.0));  // maximum valid velocity

            // Then
            assertNotNull(dto);
            assertEquals(5000.0, dto.velocity().getValue().doubleValue(), 0.01);
        }

        @Test
        @DisplayName("Should handle single shot in statistics")
        void shouldHandleSingleShotInStatistics() {
            // When
            var dto = mapper.toDto(new GroupStatisticsJsr385(
                createValidGroup(),
                getQuantity(2800.0, FEET_PER_SECOND),
                getQuantity(0.0, FEET_PER_SECOND),
                getQuantity(0.0, FEET_PER_SECOND),
                List.of(createValidShot(1L, 2800.0))
            ));

            // Then
            assertNotNull(dto);
            assertEquals(1, dto.shots().size());
            assertEquals(2800.0, dto.shots().get(0).velocity().getValue().doubleValue(), 0.01);
        }
    }
}

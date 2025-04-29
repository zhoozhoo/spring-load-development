package ca.zhoozhoo.loaddev.loads.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class LoadsServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ShotRepository shotRepository;

    @InjectMocks
    private LoadsService loadsService;

    private static final String USER_ID = randomUUID().toString();
    private static final Long GROUP_ID = 1L;
    private static final Long LOAD_ID = 1L;

    private Group createTestGroup() {
        return new Group(GROUP_ID, USER_ID, LOAD_ID, LocalDate.now(), 24.0, 100, 1.0);
    }

    private List<Shot> createTestShots() {
        return List.of(
                new Shot(1L, USER_ID, GROUP_ID, 2800),
                new Shot(2L, USER_ID, GROUP_ID, 2820),
                new Shot(3L, USER_ID, GROUP_ID, 2810));
    }

    @Test
    void whenGroupExists_thenCalculateStatisticsCorrectly() {
        Group group = createTestGroup();
        List<Shot> shots = createTestShots();

        when(groupRepository.findByIdAndOwnerId(GROUP_ID, USER_ID)).thenReturn(Mono.just(group));
        when(shotRepository.findByGroupIdAndOwnerId(GROUP_ID, USER_ID)).thenReturn(Flux.fromIterable(shots));

        StepVerifier.create(loadsService.getGroupStatistics(GROUP_ID, USER_ID))
                .assertNext(stats -> {
                    assertEquals(group, stats.group());
                    assertEquals(LOAD_ID, stats.group().loadId());
                    assertEquals(3, stats.shotCount());
                    assertEquals(2810.0, stats.averageVelocity(), 0.01);
                    assertEquals(8.16, stats.standardDeviation(), 0.01);
                    assertEquals(20.0, stats.extremeSpread(), 0.01);
                    assertEquals(shots, stats.shots());
                })
                .verifyComplete();
    }

    @Test
    void whenNoShots_thenReturnZeroStatistics() {
        Group group = createTestGroup();

        when(groupRepository.findByIdAndOwnerId(GROUP_ID, USER_ID)).thenReturn(Mono.just(group));
        when(shotRepository.findByGroupIdAndOwnerId(GROUP_ID, USER_ID)).thenReturn(Flux.empty());

        StepVerifier.create(loadsService.getGroupStatistics(GROUP_ID, USER_ID))
                .assertNext(stats -> {
                    assertEquals(0, stats.shotCount());
                    assertEquals(0.0, stats.averageVelocity());
                    assertEquals(0.0, stats.standardDeviation());
                    assertEquals(0.0, stats.extremeSpread());
                })
                .verifyComplete();
    }

    @Test
    void whenGroupNotFound_thenReturnEmpty() {
        when(groupRepository.findByIdAndOwnerId(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(loadsService.getGroupStatistics(GROUP_ID, USER_ID))
                .verifyComplete();
    }

    @Test
    void whenSingleShot_thenCalculateStatisticsCorrectly() {
        Group group = createTestGroup();
        List<Shot> shots = List.of(new Shot(1L, USER_ID, GROUP_ID, 2800));

        when(groupRepository.findByIdAndOwnerId(GROUP_ID, USER_ID)).thenReturn(Mono.just(group));
        when(shotRepository.findByGroupIdAndOwnerId(GROUP_ID, USER_ID)).thenReturn(Flux.fromIterable(shots));

        StepVerifier.create(loadsService.getGroupStatistics(GROUP_ID, USER_ID))
                .assertNext(stats -> {
                    assertEquals(1, stats.shotCount());
                    assertEquals(2800.0, stats.averageVelocity());
                    assertEquals(0.0, stats.standardDeviation());
                    assertEquals(0.0, stats.extremeSpread());
                })
                .verifyComplete();
    }
}

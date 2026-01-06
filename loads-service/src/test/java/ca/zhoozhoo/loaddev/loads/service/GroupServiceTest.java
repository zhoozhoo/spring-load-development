package ca.zhoozhoo.loaddev.loads.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.unit.Units.METRE_PER_SECOND;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.loads.mapper.GroupStatisticsMapper;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.units.indriya.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ShotRepository shotRepository;

    @Mock
    private GroupStatisticsMapper groupStatisticsMapper;

    @InjectMocks
    private GroupService groupService;

    @Test
    void getAllGroups_ShouldReturnFluxOfGroups() {
        Group group = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        when(groupRepository.findAllByLoadIdAndOwnerId(1L, "user1")).thenReturn(Flux.just(group));

        StepVerifier.create(groupService.getAllGroups(1L, "user1"))
                .expectNext(group)
                .verifyComplete();
    }

    @Test
    void getGroupById_ShouldReturnGroup_WhenFound() {
        Group group = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        when(groupRepository.findByIdAndOwnerId(1L, "user1")).thenReturn(Mono.just(group));

        StepVerifier.create(groupService.getGroupById(1L, "user1"))
                .expectNext(group)
                .verifyComplete();
    }

    @Test
    void createGroup_ShouldReturnCreatedGroup() {
        Group group = new Group(null, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        Group savedGroup = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        when(groupRepository.save(any(Group.class))).thenReturn(Mono.just(savedGroup));

        StepVerifier.create(groupService.createGroup(group))
                .expectNext(savedGroup)
                .verifyComplete();
    }

    @Test
    void updateGroup_ShouldReturnUpdatedGroup() {
        Group group = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        when(groupRepository.save(any(Group.class))).thenReturn(Mono.just(group));

        StepVerifier.create(groupService.updateGroup(group))
                .expectNext(group)
                .verifyComplete();
    }

    @Test
    void deleteGroup_ShouldComplete() {
        Group group = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        when(groupRepository.delete(group)).thenReturn(Mono.empty());

        StepVerifier.create(groupService.deleteGroup(group))
                .verifyComplete();
    }

    @Test
    void getGroupStatistics_ShouldReturnStatistics() {
        Group group = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        Shot shot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, METRE_PER_SECOND));
        GroupStatisticsDto dto = new GroupStatisticsDto(LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL), Quantities.getQuantity(1000.0, METRE_PER_SECOND), Quantities.getQuantity(0.0, METRE_PER_SECOND), Quantities.getQuantity(0.0, METRE_PER_SECOND), Collections.emptyList());

        when(groupRepository.findByIdAndOwnerId(1L, "user1")).thenReturn(Mono.just(group));
        when(shotRepository.findByGroupIdAndOwnerId(1L, "user1")).thenReturn(Flux.just(shot));
        when(groupStatisticsMapper.toDto(any(GroupStatistics.class))).thenReturn(dto);

        StepVerifier.create(groupService.getGroupStatistics(1L, "user1"))
                .expectNext(dto)
                .verifyComplete();
    }
    
    @Test
    void getGroupStatisticsForLoad_ShouldReturnFluxOfStatistics() {
        Group group = new Group(1L, "user1", 1L, LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL));
        Shot shot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, METRE_PER_SECOND));
        GroupStatisticsDto dto = new GroupStatisticsDto(LocalDate.now(), Quantities.getQuantity(40.0, GRAIN), Quantities.getQuantity(100.0, YARD_INTERNATIONAL), Quantities.getQuantity(1.0, INCH_INTERNATIONAL), Quantities.getQuantity(1000.0, METRE_PER_SECOND), Quantities.getQuantity(0.0, METRE_PER_SECOND), Quantities.getQuantity(0.0, METRE_PER_SECOND), Collections.emptyList());

        when(groupRepository.findAllByLoadIdAndOwnerId(1L, "user1")).thenReturn(Flux.just(group));
        when(shotRepository.findByGroupIdAndOwnerId(1L, "user1")).thenReturn(Flux.just(shot));
        when(groupStatisticsMapper.toDto(any(GroupStatistics.class))).thenReturn(dto);

        StepVerifier.create(groupService.getGroupStatisticsForLoad(1L, "user1"))
                .expectNext(dto)
                .verifyComplete();
    }
}

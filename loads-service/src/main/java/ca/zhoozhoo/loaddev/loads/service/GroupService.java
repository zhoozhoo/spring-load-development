package ca.zhoozhoo.loaddev.loads.service;

import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static tech.units.indriya.unit.Units.SECOND;

import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.loads.mapper.GroupStatisticsMapper;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Group entities.
 * <p>
 * This service provides reactive operations for Groups.
 * It handles CRUD operations ensuring data isolation by user ID.
 * It also calculates group statistics based on shot data.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class GroupService {

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> DEFAULT_VELOCITY_UNIT = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);

    private final GroupRepository groupRepository;
    private final ShotRepository shotRepository;
    private final GroupStatisticsMapper groupStatisticsMapper;

    /**
     * Constructs a new GroupService with required repository and mapper.
     *
     * @param groupRepository       the repository for Group entities
     * @param shotRepository        the repository for Shot entities
     * @param groupStatisticsMapper the mapper for group statistics
     */
    public GroupService(GroupRepository groupRepository, ShotRepository shotRepository, GroupStatisticsMapper groupStatisticsMapper) {
        this.groupRepository = groupRepository;
        this.shotRepository = shotRepository;
        this.groupStatisticsMapper = groupStatisticsMapper;
    }

    /**
     * Retrieves all groups for a specific load and user.
     *
     * @param loadId the ID of the load
     * @param userId the ID of the user
     * @return a Flux of Group entities
     */
    public Flux<Group> getAllGroups(Long loadId, String userId) {
        return groupRepository.findAllByLoadIdAndOwnerId(loadId, userId);
    }

    /**
     * Retrieves a specific group by ID and user ID.
     *
     * @param id     the ID of the group
     * @param userId the ID of the user
     * @return a Mono containing the Group if found, or empty
     */
    public Mono<Group> getGroupById(Long id, String userId) {
        return groupRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new group.
     *
     * @param group the Group entity to create
     * @return a Mono containing the created Group
     */
    public Mono<Group> createGroup(Group group) {
        return groupRepository.save(group);
    }

    /**
     * Updates an existing group.
     *
     * @param group the Group entity to update
     * @return a Mono containing the updated Group
     */
    public Mono<Group> updateGroup(Group group) {
        return groupRepository.save(group);
    }

    /**
     * Deletes a group.
     *
     * @param group the Group entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deleteGroup(Group group) {
        return groupRepository.delete(group);
    }

    /**
     * Retrieves statistics for a specific group belonging to a user.
     *
     * @param groupId the ID of the group
     * @param userId  the ID of the user (owner)
     * @return a Mono emitting the GroupStatisticsDto, or empty if not found
     */
    public Mono<GroupStatisticsDto> getGroupStatistics(Long groupId, String userId) {
        return groupRepository.findByIdAndOwnerId(groupId, userId)
                .flatMap(group -> shotRepository.findByGroupIdAndOwnerId(groupId, userId)
                        .collectList()
                        .map(shots -> buildGroupStatistics(group, shots)))
                .map(groupStatisticsMapper::toDto);
    }

    /**
     * Retrieves statistics for all groups associated with a specific load and user.
     *
     * @param loadId the ID of the load
     * @param userId the ID of the user (owner)
     * @return a Flux emitting GroupStatisticsDto for each group
     */
    public Flux<GroupStatisticsDto> getGroupStatisticsForLoad(Long loadId, String userId) {
        return groupRepository.findAllByLoadIdAndOwnerId(loadId, userId)
                .flatMap(group -> shotRepository.findByGroupIdAndOwnerId(group.id(), userId)
                        .collectList()
                        .map(shots -> buildGroupStatistics(group, shots)))
                .map(groupStatisticsMapper::toDto);
    }

    /**
     * Builds GroupStatistics from a group and its list of shots.
     * <p>
     * This method uses a single-pass algorithm to compute all statistics efficiently,
     * replacing implementations that make multiple separate stream passes.
     * </p>
     *
     * @param group the group entity
     * @param shots the list of shots associated with the group
     * @return the computed GroupStatistics
     */
    private GroupStatistics buildGroupStatistics(Group group, List<Shot> shots) {
        // Determine the unit from the first shot, or use default if no shots
        var velocityUnit = shots.isEmpty() 
            ? DEFAULT_VELOCITY_UNIT 
            : shots.get(0).velocity().getUnit().asType(Speed.class);

        // Single-pass statistics computation - much more efficient than multiple separate stream operations
        var stats = VelocityStatisticsGatherer.compute(
                shots.stream().map(Shot::velocity).toList(),
                velocityUnit
        );

        return new GroupStatistics(
                group,
                stats.average(),
                stats.standardDeviation(),
                stats.extremeSpread(),
                shots);
    }
}

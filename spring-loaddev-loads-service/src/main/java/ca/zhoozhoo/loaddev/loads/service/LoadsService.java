package ca.zhoozhoo.loaddev.loads.service;

import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.springframework.beans.factory.annotation.Autowired;
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

import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static tech.units.indriya.unit.Units.SECOND;

/**
 * Service class for handling operations related to Loads, Groups, and Shots.
 * <p>
 * This service provides business logic for calculating ballistic statistics from shooting data
 * with type-safe unit handling. It computes average velocity, standard deviation, and extreme spread
 * using {@link javax.measure.Quantity} types. The service coordinates between repository layers and 
 * mappers to transform domain models into DTOs for API responses.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class LoadsService {

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> DEFAULT_VELOCITY_UNIT = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupStatisticsMapper groupStatisticsMapper;

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

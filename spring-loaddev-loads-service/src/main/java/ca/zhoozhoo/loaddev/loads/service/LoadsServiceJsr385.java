package ca.zhoozhoo.loaddev.loads.service;

import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.loads.dao.GroupJsr385Repository;
import ca.zhoozhoo.loaddev.loads.dao.ShotJsr385Repository;
import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsJsr385Dto;
import ca.zhoozhoo.loaddev.loads.mapper.GroupStatisticsJsr385Mapper;
import ca.zhoozhoo.loaddev.loads.model.GroupJsr385;
import ca.zhoozhoo.loaddev.loads.model.GroupStatisticsJsr385;
import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static tech.units.indriya.unit.Units.SECOND;

/**
 * Service class for handling operations related to Loads, Groups, and Shots using JSR-385 Quantity API.
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
public class LoadsServiceJsr385 {

    @SuppressWarnings("unchecked")
    private static final Unit<Speed> DEFAULT_VELOCITY_UNIT = (Unit<Speed>) FOOT_INTERNATIONAL.divide(SECOND);

    @Autowired
    private GroupJsr385Repository groupRepository;

    @Autowired
    private ShotJsr385Repository shotRepository;

    @Autowired
    private GroupStatisticsJsr385Mapper groupStatisticsMapper;

    /**
     * Retrieves statistics for a specific group belonging to a user.
     *
     * @param groupId the ID of the group
     * @param userId  the ID of the user (owner)
     * @return a Mono emitting the GroupStatisticsJsr385Dto, or empty if not found
     */
    public Mono<GroupStatisticsJsr385Dto> getGroupStatistics(Long groupId, String userId) {
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
     * @return a Flux emitting GroupStatisticsJsr385Dto for each group
     */
    public Flux<GroupStatisticsJsr385Dto> getGroupStatisticsForLoad(Long loadId, String userId) {
        return groupRepository.findAllByLoadIdAndOwnerId(loadId, userId)
                .flatMap(group -> shotRepository.findByGroupIdAndOwnerId(group.id(), userId)
                        .collectList()
                        .map(shots -> buildGroupStatistics(group, shots)))
                .map(groupStatisticsMapper::toDto);
    }

    /**
     * Builds GroupStatisticsJsr385 from a group and its list of shots.
     * <p>
     * This method uses a single-pass algorithm to compute all statistics efficiently,
     * replacing implementations that make multiple separate stream passes.
     * This is a performance optimization enabled by Java 25 best practices with
     * JSR-385 Quantity API for type-safe unit handling.
     * </p>
     *
     * @param group the group entity
     * @param shots the list of shots associated with the group
     * @return the computed GroupStatisticsJsr385
     */
    private GroupStatisticsJsr385 buildGroupStatistics(GroupJsr385 group, List<ShotJsr385> shots) {
        // Determine the unit from the first shot, or use default if no shots
        var velocityUnit = shots.isEmpty() 
            ? DEFAULT_VELOCITY_UNIT 
            : shots.get(0).velocity().getUnit().asType(Speed.class);

        // Single-pass statistics computation - much more efficient than multiple separate stream operations
        var stats = VelocityStatisticsGathererJsr385.compute(
                shots.stream().map(ShotJsr385::velocity).toList(),
                velocityUnit
        );

        return new GroupStatisticsJsr385(
                group,
                stats.average(),
                stats.standardDeviation(),
                stats.extremeSpread(),
                shots);
    }
}

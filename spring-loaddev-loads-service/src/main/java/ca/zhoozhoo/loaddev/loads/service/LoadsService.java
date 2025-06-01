package ca.zhoozhoo.loaddev.loads.service;

import java.util.List;

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

/**
 * Service class for handling operations related to Loads, Groups, and Shots.
 */
@Service
public class LoadsService {

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
     * @return a Mono emitting the GroupStatistics, or empty if not found
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
     * @return a Flux emitting GroupStatistics for each group
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
     *
     * @param group the group entity
     * @param shots the list of shots associated with the group
     * @return the computed GroupStatistics
     */
    private GroupStatistics buildGroupStatistics(Group group, List<Shot> shots) {
        double avg = shots.stream()
                .mapToDouble(Shot::velocity)
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(shots.stream()
                .mapToDouble(shot -> Math.pow(shot.velocity() - avg, 2))
                .average()
                .orElse(0.0));

        double maxVel = shots.stream()
                .mapToDouble(Shot::velocity)
                .max()
                .orElse(0.0);
        double minVel = shots.stream()
                .mapToDouble(Shot::velocity)
                .min()
                .orElse(0.0);
        double extremeSpread = maxVel - minVel;

        return new GroupStatistics(
                group,
                avg,
                stdDev,
                extremeSpread,
                shots);
    }
}

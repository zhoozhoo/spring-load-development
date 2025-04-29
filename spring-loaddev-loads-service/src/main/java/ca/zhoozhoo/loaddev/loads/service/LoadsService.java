package ca.zhoozhoo.loaddev.loads.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
import reactor.core.publisher.Mono;

@Service
public class LoadsService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ShotRepository shotRepository;

    public Mono<GroupStatistics> getGroupStatistics(Long groupId, String userId) {
        return groupRepository.findByIdAndOwnerId(groupId, userId)
                .flatMap(group -> shotRepository.findByGroupIdAndOwnerId(groupId, userId)
                        .collectList()
                        .map(shots -> {
                            // Calculate standard deviation
                            double avg = shots.stream()
                                    .mapToDouble(shot -> shot.velocity())
                                    .average()
                                    .orElse(0.0);

                            double stdDev = Math.sqrt(shots.stream()
                                    .mapToDouble(shot -> Math.pow(shot.velocity() - avg, 2))
                                    .average()
                                    .orElse(0.0));

                            // Calculate extreme spread
                            double maxVel = shots.stream()
                                    .mapToDouble(shot -> shot.velocity())
                                    .max()
                                    .orElse(0.0);
                            double minVel = shots.stream()
                                    .mapToDouble(shot -> shot.velocity())
                                    .min()
                                    .orElse(0.0);
                            double extremeSpread = maxVel - minVel;

                            return new GroupStatistics(
                                    group,
                                    shots.size(),
                                    avg,
                                    stdDev,
                                    extremeSpread,
                                    shots);
                        }));
    }
}

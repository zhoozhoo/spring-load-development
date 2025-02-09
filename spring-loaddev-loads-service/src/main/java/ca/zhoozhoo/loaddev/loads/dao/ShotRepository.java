package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;

public interface ShotRepository extends ReactiveCrudRepository<Shot, Long> {

    Flux<Shot> findByGroupId(Long groupId);
}

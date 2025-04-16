package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShotRepository extends ReactiveCrudRepository<Shot, Long> {

    Flux<Shot> findByGroupIdAndOwnerId(Long groupId, String ownerId);

    Mono<Shot> findByIdAndOwnerId(Long id, String ownerId);
}

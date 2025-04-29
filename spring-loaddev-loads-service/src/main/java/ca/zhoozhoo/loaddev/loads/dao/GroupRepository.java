package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.Group;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupRepository extends R2dbcRepository<Group, Long> {

    Flux<Group> findAllByLoadIdAndOwnerId(Long loadId, String ownerId);

    Mono<Group> findByIdAndOwnerId(Long id, String ownerId);
}
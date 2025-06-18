package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.components.model.Case;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CaseRepository extends ReactiveCrudRepository<Case, Long> {
    
    Flux<Case> findAllByOwnerId(String ownerId);

    Mono<Case> findByIdAndOwnerId(Long id, String ownerId);
}

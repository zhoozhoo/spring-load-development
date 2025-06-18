package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.components.model.Primer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PrimerRepository extends ReactiveCrudRepository<Primer, Long> {
    
    Flux<Primer> findAllByOwnerId(String ownerId);

    Mono<Primer> findByIdAndOwnerId(Long id, String ownerId);
}

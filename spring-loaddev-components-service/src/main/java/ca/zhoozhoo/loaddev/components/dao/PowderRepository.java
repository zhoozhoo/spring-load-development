package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.components.model.Powder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PowderRepository extends R2dbcRepository<Powder, Long> {

    Flux<Powder> findAllByOwnerId(String ownerId);

    Mono<Powder> findByIdAndOwnerId(Long id, String ownerId);
}

package ca.zhoozhoo.loaddev.rifles.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RifleRepository extends R2dbcRepository<Rifle, Long> {

    Flux<Rifle> findAllByOwnerId(String ownerId);

    Mono<Rifle> findByIdAndOwnerId(Long id, String ownerId);
}

package ca.zhoozhoo.loaddev.rifles.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Reactive repository for [Rifle] entity operations.
///
/// Provides CRUD operations with owner-based filtering for multi-tenant data isolation.
///
/// @author Zhubin Salehi
public interface RifleRepository extends R2dbcRepository<Rifle, Long> {

    Flux<Rifle> findAllByOwnerId(String ownerId, Pageable pageable);

    Mono<Rifle> findByIdAndOwnerId(Long id, String ownerId);
}

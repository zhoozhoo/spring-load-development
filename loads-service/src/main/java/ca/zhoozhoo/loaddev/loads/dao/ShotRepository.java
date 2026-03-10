package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Reactive repository interface for [Shot] entity operations.
///
/// Provides CRUD operations and custom query methods for individual shot data
/// using javax.measure Quantity API for velocity measurements. Supports owner-based
/// and group-based filtering for secure multi-tenant data access.
///
/// @author Zhubin Salehi
public interface ShotRepository extends R2dbcRepository<Shot, Long> {

    Flux<Shot> findByGroupIdAndOwnerId(Long groupId, String ownerId, Pageable pageable);

    Flux<Shot> findByGroupIdAndOwnerId(Long groupId, String ownerId);

    Mono<Shot> findByIdAndOwnerId(Long id, String ownerId);
}

package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.Group;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Reactive repository interface for [Group] entity operations.
///
/// Provides CRUD operations and custom query methods for shooting group data using
/// javax.measure Quantity API. Groups represent collections of shots fired with
/// a specific load configuration. All queries support owner-based filtering for
/// secure multi-tenant access.
///
/// @author Zhubin Salehi
public interface GroupRepository extends R2dbcRepository<Group, Long> {

    Flux<Group> findAllByLoadIdAndOwnerId(Long loadId, String ownerId, Pageable pageable);

    Flux<Group> findAllByLoadIdAndOwnerId(Long loadId, String ownerId);

    Mono<Group> findByIdAndOwnerId(Long id, String ownerId);
}

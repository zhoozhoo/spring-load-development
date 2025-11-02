package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.Group;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link Group} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for shooting group data,
 * which represents a collection of shots fired with a specific load configuration.
 * All queries support owner-based filtering for secure multi-tenant access.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface GroupRepository extends R2dbcRepository<Group, Long> {

    Flux<Group> findAllByLoadIdAndOwnerId(Long loadId, String ownerId);

    Mono<Group> findByIdAndOwnerId(Long id, String ownerId);
}
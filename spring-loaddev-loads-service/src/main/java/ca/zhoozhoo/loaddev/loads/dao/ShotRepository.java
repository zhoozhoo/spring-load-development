package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link Shot} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for individual shot data,
 * including velocity measurements. Supports owner-based and group-based filtering
 * for secure multi-tenant data access.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface ShotRepository extends R2dbcRepository<Shot, Long> {

    Flux<Shot> findByGroupIdAndOwnerId(Long groupId, String ownerId);

    Mono<Shot> findByIdAndOwnerId(Long id, String ownerId);
}

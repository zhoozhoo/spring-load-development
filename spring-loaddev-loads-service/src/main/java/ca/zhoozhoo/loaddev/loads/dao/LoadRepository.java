package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link Load} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for ammunition load data,
 * supporting reactive, non-blocking database access with owner-based filtering
 * for multi-tenant data isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface LoadRepository extends R2dbcRepository<Load, Long> {

    Flux<Load> findAllByOwnerId(String ownerId);

    Flux<Load> findByNameAndOwnerId(String name, String ownerId);

    Mono<Load> findByIdAndOwnerId(Long id, String ownerId);
}

package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.LoadJsr385;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link LoadJsr385} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for ammunition load data,
 * supporting reactive, non-blocking database access with owner-based filtering
 * for multi-tenant data isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface LoadJsr385Repository extends R2dbcRepository<LoadJsr385, Long> {

    Flux<LoadJsr385> findAllByOwnerId(String ownerId);

    Flux<LoadJsr385> findByNameAndOwnerId(String name, String ownerId);

    Mono<LoadJsr385> findByIdAndOwnerId(Long id, String ownerId);
}

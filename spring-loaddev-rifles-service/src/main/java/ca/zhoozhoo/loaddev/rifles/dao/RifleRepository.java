package ca.zhoozhoo.loaddev.rifles.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link Rifle} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for rifle firearm data,
 * supporting reactive, non-blocking database access with owner-based filtering
 * for multi-tenant data isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface RifleRepository extends R2dbcRepository<Rifle, Long> {

    Flux<Rifle> findAllByOwnerId(String ownerId);

    Mono<Rifle> findByIdAndOwnerId(Long id, String ownerId);
}

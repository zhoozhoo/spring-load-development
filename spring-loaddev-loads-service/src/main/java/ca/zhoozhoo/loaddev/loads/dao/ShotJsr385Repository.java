package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link ShotJsr385} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for individual shot data
 * using javax.measure Quantity API for velocity measurements. Supports owner-based
 * and group-based filtering for secure multi-tenant data access.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface ShotJsr385Repository extends R2dbcRepository<ShotJsr385, Long> {

    Flux<ShotJsr385> findByGroupIdAndOwnerId(Long groupId, String ownerId);

    Mono<ShotJsr385> findByIdAndOwnerId(Long id, String ownerId);
}

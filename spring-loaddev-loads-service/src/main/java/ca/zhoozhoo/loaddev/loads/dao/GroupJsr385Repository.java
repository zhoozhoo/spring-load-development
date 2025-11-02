package ca.zhoozhoo.loaddev.loads.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.loads.model.GroupJsr385;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository interface for {@link GroupJsr385} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for shooting group data using
 * javax.measure Quantity API. Groups represent collections of shots fired with
 * a specific load configuration. All queries support owner-based filtering for
 * secure multi-tenant access.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface GroupJsr385Repository extends R2dbcRepository<GroupJsr385, Long> {

    Flux<GroupJsr385> findAllByLoadIdAndOwnerId(Long loadId, String ownerId);

    Mono<GroupJsr385> findByIdAndOwnerId(Long id, String ownerId);
}

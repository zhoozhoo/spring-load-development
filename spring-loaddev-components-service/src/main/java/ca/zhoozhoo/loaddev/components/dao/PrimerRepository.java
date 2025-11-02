package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.components.model.Primer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for managing {@link Primer} entities.
 * <p>
 * Provides reactive data access operations for primer components including
 * multi-tenant filtering by owner ID and full-text search capabilities using
 * PostgreSQL's text search features.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface PrimerRepository extends ReactiveCrudRepository<Primer, Long> {
    
    Flux<Primer> findAllByOwnerId(String ownerId);

    Mono<Primer> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT * FROM primers WHERE owner_id = :ownerId AND search_vector @@ plainto_tsquery('simple', :q) "
            + "ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :q)) DESC")
    Flux<Primer> searchByOwnerIdAndQuery(@Param("ownerId") String ownerId, @Param("q") String query);
}

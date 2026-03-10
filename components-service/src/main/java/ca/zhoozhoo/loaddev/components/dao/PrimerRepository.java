package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.components.model.Primer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Reactive repository for Primer entities with multi-tenant filtering and PostgreSQL full-text search.
///
/// @author Zhubin Salehi
public interface PrimerRepository extends ReactiveCrudRepository<Primer, Long> {
    
    Flux<Primer> findAllByOwnerId(String ownerId, Pageable pageable);

    Mono<Primer> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT * FROM primers WHERE owner_id = :ownerId AND search_vector @@ plainto_tsquery('simple', :q) "
            + "ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :q)) DESC "
            + "LIMIT :limit OFFSET :offset")
    Flux<Primer> searchByOwnerIdAndQuery(@Param("ownerId") String ownerId, @Param("q") String query,
            @Param("limit") int limit, @Param("offset") long offset);
}

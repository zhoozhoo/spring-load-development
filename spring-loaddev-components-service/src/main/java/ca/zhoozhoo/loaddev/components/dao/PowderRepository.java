package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.components.model.Powder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive R2DBC repository for managing {@link Powder} entities.
 * <p>
 * Provides reactive data access operations for powder propellant components including
 * multi-tenant filtering by owner ID and full-text search capabilities using
 * PostgreSQL's text search features.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface PowderRepository extends R2dbcRepository<Powder, Long> {

    Flux<Powder> findAllByOwnerId(String ownerId);

    Mono<Powder> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT * FROM powders WHERE owner_id = :ownerId AND search_vector @@ plainto_tsquery('simple', :q) "
            + "ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :q)) DESC")
    Flux<Powder> searchByOwnerIdAndQuery(@Param("ownerId") String ownerId, @Param("q") String query);
}

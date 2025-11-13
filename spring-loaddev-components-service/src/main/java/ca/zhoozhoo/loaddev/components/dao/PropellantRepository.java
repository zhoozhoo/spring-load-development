package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import ca.zhoozhoo.loaddev.components.model.Propellant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive R2DBC repository for managing {@link Propellant} entities.
 * <p>
 * Provides reactive data access operations for propellant components including
 * multi-tenant filtering by owner ID and full-text search capabilities using
 * PostgreSQL's text search features.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface PropellantRepository extends R2dbcRepository<Propellant, Long> {

    Flux<Propellant> findAllByOwnerId(String ownerId);

    Mono<Propellant> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT * FROM propellants WHERE owner_id = :ownerId AND search_vector @@ plainto_tsquery('simple', :q) "
        + "ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :q)) DESC")
    Flux<Propellant> searchByOwnerIdAndQuery(@Param("ownerId") String ownerId, @Param("q") String query);
}

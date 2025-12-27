package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import ca.zhoozhoo.loaddev.components.model.Propellant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for Propellant entities with multi-tenant filtering and PostgreSQL full-text search.
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

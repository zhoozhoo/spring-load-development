package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.zhoozhoo.loaddev.components.model.Bullet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive R2DBC repository for managing {@link Bullet} entities.
 * <p>
 * Provides reactive data access operations for bullet components including
 * multi-tenant filtering by owner ID and full-text search capabilities using
 * PostgreSQL's text search features.
 * </p>
 *
 * @author Zhubin Salehi
 */
public interface BulletRepository extends R2dbcRepository<Bullet, Long> {

    Flux<Bullet> findAllByOwnerId(String ownerId);

    Mono<Bullet> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT * FROM bullets WHERE owner_id = :ownerId AND search_vector @@ plainto_tsquery('english', :q) "
        + "ORDER BY ts_rank(search_vector, plainto_tsquery('english', :q)) DESC")
    Flux<Bullet> searchByOwnerIdAndQuery(@Param("ownerId") String ownerId, @Param("q") String query);
}

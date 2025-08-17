package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ca.zhoozhoo.loaddev.components.model.Case;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CaseRepository extends ReactiveCrudRepository<Case, Long> {
    
    Flux<Case> findAllByOwnerId(String ownerId);

    Mono<Case> findByIdAndOwnerId(Long id, String ownerId);

    @Query("SELECT * FROM cases WHERE owner_id = :ownerId AND search_vector @@ plainto_tsquery('simple', :q) "
            + "ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :q)) DESC")
    Flux<Case> searchByOwnerIdAndQuery(@Param("ownerId") String ownerId, @Param("q") String query);
}

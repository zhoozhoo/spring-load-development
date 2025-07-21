package ca.zhoozhoo.loaddev.components.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.components.model.Bullet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BulletRepository extends R2dbcRepository<Bullet, Long> {

    Flux<Bullet> findAllByOwnerId(String ownerId);

    Mono<Bullet> findByIdAndOwnerId(Long id, String ownerId);
}

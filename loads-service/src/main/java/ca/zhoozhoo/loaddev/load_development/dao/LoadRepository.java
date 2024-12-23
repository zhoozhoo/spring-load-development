package ca.zhoozhoo.loaddev.load_development.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.load_development.model.Load;
import reactor.core.publisher.Flux;

public interface LoadRepository extends R2dbcRepository<Load, Long> {

    Flux<Load> findByName(String name);
}

package ca.zhoozhoo.loaddev.load_development.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.load_development.model.Load;

public interface LoadRepository extends R2dbcRepository<Load, Long> {

}

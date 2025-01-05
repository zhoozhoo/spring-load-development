package ca.zhoozhoo.loaddev.load_development.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.load_development.model.Rifle;

public interface RifleRepository extends R2dbcRepository<Rifle, Long> {

}

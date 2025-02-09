package ca.zhoozhoo.loaddev.rifles.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.rifles.model.Rifle;

public interface RifleRepository extends R2dbcRepository<Rifle, Long> {

}

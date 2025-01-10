package ca.zhoozhoo.loaddev.load_development.dao;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import ca.zhoozhoo.loaddev.load_development.model.Group;

public interface GroupRepository extends R2dbcRepository<Group, Long> {
}
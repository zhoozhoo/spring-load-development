CREATE TABLE IF NOT EXISTS loads (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    powder_manufacturer VARCHAR(255) NOT NULL,
    powder_type VARCHAR(255) NOT NULL,
    bullet_manufacturer VARCHAR(255) NOT NULL,
    bullet_type VARCHAR(255) NOT NULL,
    bullet_weight DOUBLE PRECISION NOT NULL,
    bullet_weight_unit VARCHAR(32) NOT NULL,
    primer_manufacturer VARCHAR(255) NOT NULL,
    primer_type VARCHAR(255) NOT NULL,
    distance_from_lands DOUBLE PRECISION,
    distance_from_lands_unit VARCHAR(32),
    case_overall_length DOUBLE PRECISION,
    case_overall_length_unit VARCHAR(32),
    neck_tension DOUBLE PRECISION,
    neck_tension_unit VARCHAR(32),
    rifle_id BIGSERIAL,
    CONSTRAINT chk_measurement CHECK (
        distance_from_lands IS NOT NULL OR case_overall_length IS NOT NULL
    )
);

CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    powder_charge DOUBLE PRECISION NOT NULL,
    powder_charge_unit VARCHAR(32) NOT NULL,
    target_range INTEGER NOT NULL,
    target_range_unit VARCHAR(32) NOT NULL,
    group_size DOUBLE PRECISION,
    group_size_unit VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS shots (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    group_id BIGSERIAL NOT NULL,
    velocity INTEGER,
    velocity_unit VARCHAR(32),
    CONSTRAINT fk_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
);
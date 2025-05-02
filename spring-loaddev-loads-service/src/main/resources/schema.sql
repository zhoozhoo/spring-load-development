CREATE TABLE IF NOT EXISTS loads (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    measurement_units VARCHAR(32) NOT NULL check (measurement_units in ('Imperial','Metric')),
    powder_manufacturer VARCHAR(255) NOT NULL,
    powder_type VARCHAR(255) NOT NULL,
    bullet_manufacturer VARCHAR(255) NOT NULL,
    bullet_type VARCHAR(255) NOT NULL,
    bullet_weight DOUBLE PRECISION NOT NULL,
    primer_manufacturer VARCHAR(255) NOT NULL,
    primer_type VARCHAR(255) NOT NULL,
    distance_from_lands DOUBLE PRECISION,
    case_overall_length DOUBLE PRECISION,
    neck_tension DOUBLE PRECISION,
    rifle_id BIGSERIAL,
    CONSTRAINT chk_measurement CHECK (
        distance_from_lands IS NOT NULL OR case_overall_length IS NOT NULL
    )
);

CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    load_id BIGSERIAL NOT NULL,
    date DATE NOT NULL,
    powder_charge DOUBLE PRECISION NOT NULL,
    target_range INTEGER NOT NULL,
    group_size DOUBLE PRECISION,
    CONSTRAINT fk_load
        FOREIGN KEY (load_id)
        REFERENCES loads(id)
);

CREATE TABLE IF NOT EXISTS shots (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    group_id BIGSERIAL NOT NULL,
    velocity INTEGER,
    CONSTRAINT fk_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
);
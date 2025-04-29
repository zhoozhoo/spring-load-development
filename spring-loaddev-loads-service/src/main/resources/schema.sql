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
    bullet_weight_unit VARCHAR(32) NOT NULL check (bullet_weight_unit in ('gr','g')),
    primer_manufacturer VARCHAR(255) NOT NULL,
    primer_type VARCHAR(255) NOT NULL,
    distance_from_lands DOUBLE PRECISION,
    distance_from_lands_unit VARCHAR(32) check (distance_from_lands_unit in ('in','mm')),
    case_overall_length DOUBLE PRECISION,
    case_overall_length_unit VARCHAR(32) check (case_overall_length_unit in ('in','mm')),
    neck_tension DOUBLE PRECISION,
    neck_tension_unit VARCHAR(32) check (neck_tension_unit in ('in','mm')),
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
    powder_charge_unit VARCHAR(32) NOT NULL check (powder_charge_unit in ('gr','g')),
    target_range INTEGER NOT NULL,
    target_range_unit VARCHAR(32) NOT NULL,
    group_size DOUBLE PRECISION,
    group_size_unit VARCHAR(32) check (group_size_unit in ('in','mm')),
    CONSTRAINT fk_load
        FOREIGN KEY (load_id)
        REFERENCES loads(id)
);

CREATE TABLE IF NOT EXISTS shots (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    group_id BIGSERIAL NOT NULL,
    velocity INTEGER,
    velocity_unit VARCHAR(32) check (velocity_unit in ('fps','mps')),
    CONSTRAINT fk_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
);
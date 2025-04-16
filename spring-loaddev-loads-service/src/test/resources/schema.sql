CREATE TABLE IF NOT EXISTS rifles (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    caliber VARCHAR(32) NOT NULL,
    barrel_length DOUBLE PRECISION,
    barrel_contour VARCHAR(32),
    twist_rate  VARCHAR(32),
    free_bore DOUBLE PRECISION,
    rifling VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS loads (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    powder_manufacturer VARCHAR(255) NOT NULL,
    powder_type VARCHAR(255) NOT NULL,
    powder_charge DOUBLE PRECISION NOT NULL,
    bullet_manufacturer VARCHAR(255) NOT NULL,
    bullet_type VARCHAR(255) NOT NULL,
    bullet_weight DOUBLE PRECISION NOT NULL,
    primer_manufacturer VARCHAR(255) NOT NULL,
    primer_type VARCHAR(255) NOT NULL,
    distance_from_lands DOUBLE PRECISION NOT NULL,
    rifle_id BIGSERIAL NOT NULL,
    CONSTRAINT fk_rifle
        FOREIGN KEY (rifle_id)
        REFERENCES rifles(id)
);

CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    number_of_shots INTEGER NOT NULL,
    target_range INTEGER NOT NULL,
    group_size DOUBLE PRECISION NOT NULL,
    mean DOUBLE PRECISION,
    median DOUBLE PRECISION,
    min DOUBLE PRECISION,
    max DOUBLE PRECISION,
    standard_deviation DOUBLE PRECISION,
    extreme_spread DOUBLE PRECISION
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
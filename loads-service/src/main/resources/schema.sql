CREATE TABLE rifles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    caliber VARCHAR(32) NOT NULL,
    barrel_length DOUBLE PRECISION,
    barrel_contour VARCHAR(32),
    twist_rate  VARCHAR(32),
    free_bore DOUBLE PRECISION,
    rifling VARCHAR(32)
);

CREATE TABLE loads (
    id BIGSERIAL PRIMARY KEY,
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
    rifle_id BIGINT NOT NULL,
    CONSTRAINT fk_rifle
        FOREIGN KEY (rifle_id)
        REFERENCES rifles(id)
);
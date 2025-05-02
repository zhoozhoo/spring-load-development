CREATE TABLE IF NOT EXISTS rifles (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    measurement_units VARCHAR(32) NOT NULL check (measurement_units in ('Imperial','Metric')),
    caliber VARCHAR(32) NOT NULL,
    barrel_length DOUBLE PRECISION,
    barrel_contour VARCHAR(32),
    twist_rate  VARCHAR(32),
    rifling VARCHAR(32),
    free_bore DOUBLE PRECISION
);

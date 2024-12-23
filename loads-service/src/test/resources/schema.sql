CREATE TABLE loads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DOUBLE PRECISION
);

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

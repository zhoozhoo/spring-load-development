CREATE TABLE IF NOT EXISTS rifles (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    caliber VARCHAR(32) NOT NULL,
    barrel_length DOUBLE PRECISION,
    barrel_length_unit VARCHAR(8) check (barrel_length_unit in ('in','mm')),
    barrel_contour VARCHAR(32),
    twist_rate  VARCHAR(32),
    rifling VARCHAR(32),
    free_bore DOUBLE PRECISION,
    free_bore_unit VARCHAR(8) check (free_bore_unit in ('in','mm'))
);

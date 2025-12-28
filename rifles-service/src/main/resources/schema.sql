CREATE TABLE IF NOT EXISTS rifles (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    caliber VARCHAR(32) NOT NULL,
    barrel_length JSONB,
    barrel_contour VARCHAR(32),
    rifling JSONB,
    zeroing JSONB
);

COMMENT ON COLUMN rifles.barrel_length IS 'JSR-385 Quantity<Length> stored as JSONB with value and unit properties';
COMMENT ON COLUMN rifles.rifling IS 'Rifling specifications stored as JSONB with twistRate (Quantity<Length>) and twistDirection properties';
COMMENT ON COLUMN rifles.zeroing IS 'Zeroing configuration stored as JSONB with sightHeight and zeroDistance (Quantity<Length>) properties';

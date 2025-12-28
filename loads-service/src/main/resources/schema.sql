-- This table stores measurement data with unit information in JSONB columns
-- Example JSONB format: {"value": 150, "unit": "gr"} or {"value": 0.020, "unit": "in"}
-- Supported via javax.measure.Quantity types and custom R2DBC converters
CREATE TABLE IF NOT EXISTS loads (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    powder_manufacturer VARCHAR(255) NOT NULL,
    powder_type VARCHAR(255) NOT NULL,
    bullet_manufacturer VARCHAR(255) NOT NULL,
    bullet_type VARCHAR(255) NOT NULL,
    bullet_weight JSONB NOT NULL,
    primer_manufacturer VARCHAR(255) NOT NULL,
    primer_type VARCHAR(255) NOT NULL,
    distance_from_lands JSONB,
    case_overall_length JSONB,
    neck_tension JSONB,
    rifle_id BIGINT,
    CONSTRAINT chk_measurement CHECK (
        distance_from_lands IS NOT NULL OR case_overall_length IS NOT NULL
    )
);

-- This table stores measurement data with unit information in JSONB columns
-- Stores Quantity<Mass> for powder_charge, Quantity<Length> for target_range and group_size
-- Supported via javax.measure.Quantity types and custom R2DBC converters
CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    load_id BIGSERIAL NOT NULL,
    date DATE NOT NULL,
    powder_charge JSONB NOT NULL,
    target_range JSONB NOT NULL,
    group_size JSONB,
    CONSTRAINT fk_load
        FOREIGN KEY (load_id)
        REFERENCES loads(id)
);

-- This table stores velocity measurement with unit information in JSONB column
-- Stores Quantity<Speed> for velocity
-- Supported via javax.measure.Quantity types and custom R2DBC converters
CREATE TABLE IF NOT EXISTS shots (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    group_id BIGSERIAL NOT NULL,
    velocity JSONB,
    CONSTRAINT fk_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
);
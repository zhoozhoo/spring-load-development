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

-- loads_jsr385 table using JSR-385 (Units of Measurement API)
-- This table stores measurement data with unit information in JSONB columns
-- Example JSONB format: {"value": 150, "unit": "gr"} or {"value": 0.020, "unit": "in"}
-- Supported via javax.measure.Quantity types and custom R2DBC converters
CREATE TABLE IF NOT EXISTS loads_jsr385 (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    powder_manufacturer VARCHAR(255) NOT NULL,
    powder_type VARCHAR(255) NOT NULL,
    bullet_manufacturer VARCHAR(255) NOT NULL,
    bullet_type VARCHAR(255) NOT NULL,
    bullet_weight JSONB NOT NULL,              -- Stores Quantity<Mass> as JSON with value and unit
    primer_manufacturer VARCHAR(255) NOT NULL,
    primer_type VARCHAR(255) NOT NULL,
    distance_from_lands JSONB,                 -- Stores Quantity<Length> as JSON with value and unit
    case_overall_length JSONB,                 -- Stores Quantity<Length> as JSON with value and unit
    neck_tension JSONB,                        -- Stores Quantity<Length> as JSON with value and unit
    rifle_id BIGSERIAL,
    CONSTRAINT chk_measurement_jsr385 CHECK (
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

-- groups_jsr385 table using JSR-385 (Units of Measurement API)
-- This table stores measurement data with unit information in JSONB columns
-- Stores Quantity<Mass> for powder_charge, Quantity<Length> for target_range and group_size
-- Supported via javax.measure.Quantity types and custom R2DBC converters
CREATE TABLE IF NOT EXISTS groups_jsr385 (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    load_id BIGSERIAL NOT NULL,
    date DATE NOT NULL,
    powder_charge JSONB NOT NULL,             -- Stores Quantity<Mass> as JSON with value and unit (e.g., grains)
    target_range JSONB NOT NULL,              -- Stores Quantity<Length> as JSON with value and unit (e.g., yards)
    group_size JSONB,                         -- Stores Quantity<Length> as JSON with value and unit (e.g., inches)
    CONSTRAINT fk_load_jsr385
        FOREIGN KEY (load_id)
        REFERENCES loads_jsr385(id)
);

-- shots_jsr385 table using JSR-385 (Units of Measurement API)
-- This table stores velocity measurement with unit information in JSONB column
-- Stores Quantity<Speed> for velocity
-- Supported via javax.measure.Quantity types and custom R2DBC converters
CREATE TABLE IF NOT EXISTS shots_jsr385 (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    group_id BIGSERIAL NOT NULL,
    velocity JSONB,                           -- Stores Quantity<Speed> as JSON with value and unit (e.g., fps)
    CONSTRAINT fk_group_jsr385
        FOREIGN KEY (group_id)
        REFERENCES groups_jsr385(id)
);
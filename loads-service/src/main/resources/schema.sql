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

-- Transactional outbox for reliable, at-least-once delivery of domain events to Kafka.
-- See ca.zhoozhoo.loaddev.common.events.outbox (shared module).
CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    topic VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    owner_id VARCHAR(255),
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events (status, id);
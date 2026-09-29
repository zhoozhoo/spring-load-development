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

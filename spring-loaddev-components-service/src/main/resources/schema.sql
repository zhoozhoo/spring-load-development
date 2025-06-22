CREATE TABLE IF NOT EXISTS bullets (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    type VARCHAR(255) NOT NULL,
    measurement_units VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity_per_box INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS powders (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    measurement_units VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2),
    currency VARCHAR(3),
    weight_per_container DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS primers (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    size VARCHAR(20) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity_per_box INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS cases (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    caliber VARCHAR(50) NOT NULL,
    primer_size VARCHAR(20) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity_per_box INTEGER NOT NULL
);

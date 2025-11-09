CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS projectiles (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    weight JSONB NOT NULL,
    type VARCHAR(255) NOT NULL,
    cost JSONB NOT NULL,
    quantity_per_box INTEGER NOT NULL,
    search_vector tsvector
);

-- Comments for JSR-385 and JSR-354 support
COMMENT ON TABLE projectiles IS 'Projectile components using JSR-385 units of measurement and JSR-354 money';
COMMENT ON COLUMN projectiles.weight IS 'Stores Quantity<Mass> as JSONB with value and unit. Example: {"value": 150, "unit": "gr"}. Supported via javax.measure.Quantity types and custom R2DBC converters';
COMMENT ON COLUMN projectiles.cost IS 'Stores MonetaryAmount as JSONB with amount and currency. Example: {"amount": 45.99, "currency": "USD"}. Supported via javax.money.MonetaryAmount and custom R2DBC converters';

CREATE OR REPLACE FUNCTION projectiles_update_search_vector() RETURNS trigger AS '
BEGIN
  NEW.search_vector :=
  setweight(to_tsvector(''english'', coalesce(NEW.manufacturer,'''')), ''A'') ||
  setweight(to_tsvector(''english'', coalesce(NEW.type,'''')), ''B'') ||
  setweight(to_tsvector(''english'', coalesce((NEW.weight->>''value'')::text,'''')), ''C'');
  RETURN NEW;
END
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS projectiles_search_vector_update ON projectiles;
CREATE TRIGGER projectiles_search_vector_update
BEFORE INSERT OR UPDATE ON projectiles
FOR EACH ROW EXECUTE FUNCTION projectiles_update_search_vector();

CREATE INDEX IF NOT EXISTS idx_projectiles_search_vector ON projectiles USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_projectiles_text_trgm ON projectiles USING GIN ((coalesce(manufacturer,'') || ' ' || coalesce(type,'')) gin_trgm_ops);

CREATE TABLE IF NOT EXISTS propellants (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    cost JSONB,
    weight_per_container JSONB,
    search_vector tsvector
);

-- Comments for JSR-385 and JSR-354 support
COMMENT ON TABLE propellants IS 'Propellant components using JSR-385 units of measurement and JSR-354 money';
COMMENT ON COLUMN propellants.weight_per_container IS 'Stores Quantity<Mass> as JSONB with value and unit. Example: {"value": 1, "unit": "lb"}. Supported via javax.measure.Quantity types and custom R2DBC converters';
COMMENT ON COLUMN propellants.cost IS 'Stores MonetaryAmount as JSONB with amount and currency. Example: {"amount": 35.99, "currency": "USD"}. Supported via javax.money.MonetaryAmount and custom R2DBC converters';

CREATE OR REPLACE FUNCTION propellants_update_search_vector() RETURNS trigger AS '
BEGIN
  NEW.search_vector := to_tsvector(''english'', coalesce(NEW.manufacturer,'''') || '' '' || coalesce(NEW.type,''''));
  RETURN NEW;
END
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS propellants_search_vector_update ON propellants;
CREATE TRIGGER propellants_search_vector_update
BEFORE INSERT OR UPDATE ON propellants
FOR EACH ROW EXECUTE FUNCTION propellants_update_search_vector();

CREATE INDEX IF NOT EXISTS idx_propellants_search_vector ON propellants USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_propellants_text_trgm ON propellants USING GIN ((coalesce(manufacturer,'') || ' ' || coalesce(type,'')) gin_trgm_ops);

CREATE TABLE IF NOT EXISTS primers (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    size VARCHAR(20) NOT NULL,
    cost JSONB NOT NULL,
    quantity_per_box JSONB NOT NULL,
    search_vector tsvector
);

-- Comments for JSR-385 and JSR-354 support
COMMENT ON TABLE primers IS 'Primer components using JSR-385 units of measurement and JSR-354 money';
COMMENT ON COLUMN primers.cost IS 'Stores MonetaryAmount as JSONB with amount and currency. Example: {"amount": 45.99, "currency": "USD"}. Supported via javax.money.MonetaryAmount and custom R2DBC converters';
COMMENT ON COLUMN primers.quantity_per_box IS 'Stores Quantity<Dimensionless> as JSONB with value and unit. Example: {"value": 100, "unit": "one"}. Supported via javax.measure.Quantity types and custom R2DBC converters';

CREATE OR REPLACE FUNCTION primers_update_search_vector() RETURNS trigger AS '
BEGIN
  NEW.search_vector := to_tsvector(''english'', coalesce(NEW.manufacturer,'''') || '' '' || coalesce(NEW.type,''''));
  RETURN NEW;
END
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS primers_search_vector_update ON primers;
CREATE TRIGGER primers_search_vector_update
BEFORE INSERT OR UPDATE ON primers
FOR EACH ROW EXECUTE FUNCTION primers_update_search_vector();

CREATE INDEX IF NOT EXISTS idx_primers_search_vector ON primers USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_primers_text_trgm ON primers USING GIN ((coalesce(manufacturer,'') || ' ' || coalesce(type,'')) gin_trgm_ops);

CREATE TABLE IF NOT EXISTS cases (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    caliber VARCHAR(50) NOT NULL,
    primer_size VARCHAR(20) NOT NULL,
    cost JSONB NOT NULL,
    quantity_per_box JSONB NOT NULL,
    search_vector tsvector
);

-- Comments for JSR-385 and JSR-354 support
COMMENT ON TABLE cases IS 'Case components using JSR-385 units of measurement and JSR-354 money';
COMMENT ON COLUMN cases.cost IS 'Stores MonetaryAmount as JSONB with amount and currency. Example: {"amount": 29.99, "currency": "USD"}. Supported via javax.money.MonetaryAmount and custom R2DBC converters';
COMMENT ON COLUMN cases.quantity_per_box IS 'Stores Quantity<Dimensionless> as JSONB with value and unit. Example: {"value": 100, "unit": "one"}. Supported via javax.measure.Quantity types and custom R2DBC converters';

CREATE OR REPLACE FUNCTION cases_update_search_vector() RETURNS trigger AS '
BEGIN
  NEW.search_vector := to_tsvector(''english'', coalesce(NEW.manufacturer,'''') || '' '' || coalesce(NEW.caliber,''''));
  RETURN NEW;
END
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS cases_search_vector_update ON cases;
CREATE TRIGGER cases_search_vector_update
BEFORE INSERT OR UPDATE ON cases
FOR EACH ROW EXECUTE FUNCTION cases_update_search_vector();

CREATE INDEX IF NOT EXISTS idx_cases_search_vector ON cases USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_cases_text_trgm ON cases USING GIN ((coalesce(manufacturer,'') || ' ' || coalesce(caliber,'')) gin_trgm_ops);

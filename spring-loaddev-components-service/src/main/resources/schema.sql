CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS bullets (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    type VARCHAR(255) NOT NULL,
    measurement_units VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity_per_box INTEGER NOT NULL,
    search_vector tsvector
);

CREATE OR REPLACE FUNCTION bullets_update_search_vector() RETURNS trigger AS '
BEGIN
  NEW.search_vector :=
  setweight(to_tsvector(''english'', coalesce(NEW.manufacturer,'''')), ''A'') ||
  setweight(to_tsvector(''english'', coalesce(NEW.type,'''')), ''B'') ||
  setweight(to_tsvector(''english'', coalesce(NEW.weight::text,'''')), ''C'');
  RETURN NEW;
END
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS bullets_search_vector_update ON bullets;
CREATE TRIGGER bullets_search_vector_update
BEFORE INSERT OR UPDATE ON bullets
FOR EACH ROW EXECUTE FUNCTION bullets_update_search_vector();

CREATE INDEX IF NOT EXISTS idx_bullets_search_vector ON bullets USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_bullets_text_trgm ON bullets USING GIN ((coalesce(manufacturer,'') || ' ' || coalesce(type,'')) gin_trgm_ops);

CREATE TABLE IF NOT EXISTS powders (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    measurement_units VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2),
    currency VARCHAR(3),
    weight_per_container DOUBLE PRECISION,
    search_vector tsvector
);

CREATE OR REPLACE FUNCTION powders_update_search_vector() RETURNS trigger AS '
BEGIN
  NEW.search_vector := to_tsvector(''english'', coalesce(NEW.manufacturer,'''') || '' '' || coalesce(NEW.type,''''));
  RETURN NEW;
END
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS powders_search_vector_update ON powders;
CREATE TRIGGER powders_search_vector_update
BEFORE INSERT OR UPDATE ON powders
FOR EACH ROW EXECUTE FUNCTION powders_update_search_vector();

CREATE INDEX IF NOT EXISTS idx_powders_search_vector ON powders USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_powders_text_trgm ON powders USING GIN ((coalesce(manufacturer,'') || ' ' || coalesce(type,'')) gin_trgm_ops);

CREATE TABLE IF NOT EXISTS primers (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    size VARCHAR(20) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity_per_box INTEGER NOT NULL,
    search_vector tsvector
);

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
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity_per_box INTEGER NOT NULL,
    search_vector tsvector
);

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

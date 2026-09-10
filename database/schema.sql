-- =============================================================================
-- Pet Grooming Platform — Multi-tenant PostgreSQL schema
-- Isolation: shared DB + shared schema + tenant_id (row-level)
-- Images: store only URLs/keys (local public path, S3, or Azure Blob)
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE user_role AS ENUM (
  'SUPER_ADMIN',
  'SALON_ADMIN',
  'EMPLOYEE',
  'CUSTOMER'
);

CREATE TYPE appointment_status AS ENUM (
  'PENDING',
  'CONFIRMED',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
  'NO_SHOW'
);

CREATE TYPE day_of_week AS ENUM (
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY'
);

CREATE TYPE pet_gender AS ENUM (
  'MALE',
  'FEMALE',
  'UNKNOWN'
);

CREATE TYPE media_type AS ENUM (
  'IMAGE',
  'VIDEO'
);

-- -----------------------------------------------------------------------------
-- 1. Tenants & salon core
-- -----------------------------------------------------------------------------

CREATE TABLE tenants (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug            VARCHAR(100) NOT NULL UNIQUE,
  name            VARCHAR(255) NOT NULL,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE salon_profile (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
  display_name    VARCHAR(255) NOT NULL,
  tagline         VARCHAR(500),
  description     TEXT,
  phone           VARCHAR(50),
  phone_display   VARCHAR(50),
  email           VARCHAR(255),
  website_url     VARCHAR(500),
  address_line1   VARCHAR(255),
  address_line2   VARCHAR(255),
  city            VARCHAR(100),
  state           VARCHAR(100),
  postal_code     VARCHAR(30),
  country         VARCHAR(100),
  timezone        VARCHAR(64) NOT NULL DEFAULT 'UTC',
  maps_search_url TEXT,
  map_embed_url   TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE salon_branding (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
  primary_color       VARCHAR(20),
  secondary_color     VARCHAR(20),
  accent_color        VARCHAR(20),
  logo_url            TEXT,
  logo_storage_key    VARCHAR(500),
  favicon_url         TEXT,
  favicon_storage_key VARCHAR(500),
  cover_image_url     TEXT,
  cover_storage_key   VARCHAR(500),
  font_family         VARCHAR(100),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE locations (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(255) NOT NULL,
  phone           VARCHAR(50),
  email           VARCHAR(255),
  address_line1   VARCHAR(255) NOT NULL,
  address_line2   VARCHAR(255),
  city            VARCHAR(100) NOT NULL,
  state           VARCHAR(100),
  postal_code     VARCHAR(30),
  country         VARCHAR(100),
  latitude        NUMERIC(10, 7),
  longitude       NUMERIC(10, 7),
  is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_locations_tenant ON locations(tenant_id);

CREATE TABLE location_working_hours (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  location_id     UUID NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
  day             day_of_week NOT NULL,
  open_time       TIME,
  close_time      TIME,
  is_closed       BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_location_day UNIQUE (location_id, day),
  CONSTRAINT chk_location_hours CHECK (
    is_closed = TRUE OR (open_time IS NOT NULL AND close_time IS NOT NULL AND open_time < close_time)
  )
);

CREATE INDEX idx_location_working_hours_tenant ON location_working_hours(tenant_id);
CREATE INDEX idx_location_working_hours_location ON location_working_hours(location_id);

-- -----------------------------------------------------------------------------
-- 2. Users
-- -----------------------------------------------------------------------------

CREATE TABLE users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email           VARCHAR(255) NOT NULL UNIQUE,
  password_hash   VARCHAR(255),
  first_name      VARCHAR(100),
  last_name       VARCHAR(100),
  phone           VARCHAR(50),
  avatar_url      TEXT,
  avatar_storage_key VARCHAR(500),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  email_verified_at TIMESTAMPTZ,
  last_login_at   TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tenant_users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role            user_role NOT NULL DEFAULT 'SALON_ADMIN',
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_tenant_user UNIQUE (tenant_id, user_id)
);

CREATE INDEX idx_tenant_users_tenant ON tenant_users(tenant_id);
CREATE INDEX idx_tenant_users_user ON tenant_users(user_id);

-- -----------------------------------------------------------------------------
-- 3. Employees
-- -----------------------------------------------------------------------------

CREATE TABLE employees (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
  location_id     UUID REFERENCES locations(id) ON DELETE SET NULL,
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  email           VARCHAR(255),
  phone           VARCHAR(50),
  title           VARCHAR(100),
  bio             TEXT,
  photo_url       TEXT,
  photo_storage_key VARCHAR(500),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  display_order   INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employees_tenant ON employees(tenant_id);
CREATE INDEX idx_employees_location ON employees(location_id);

CREATE TABLE employee_working_hours (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  day             day_of_week NOT NULL,
  start_time      TIME,
  end_time        TIME,
  is_off          BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_employee_day UNIQUE (employee_id, day),
  CONSTRAINT chk_employee_hours CHECK (
    is_off = TRUE OR (start_time IS NOT NULL AND end_time IS NOT NULL AND start_time < end_time)
  )
);

CREATE INDEX idx_employee_working_hours_tenant ON employee_working_hours(tenant_id);
CREATE INDEX idx_employee_working_hours_employee ON employee_working_hours(employee_id);

CREATE TABLE employee_time_blocks (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  starts_at       TIMESTAMPTZ NOT NULL,
  ends_at         TIMESTAMPTZ NOT NULL,
  reason          VARCHAR(255),
  is_all_day      BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_time_block_range CHECK (starts_at < ends_at)
);

CREATE INDEX idx_employee_time_blocks_tenant ON employee_time_blocks(tenant_id);
CREATE INDEX idx_employee_time_blocks_employee ON employee_time_blocks(employee_id);
CREATE INDEX idx_employee_time_blocks_range ON employee_time_blocks(employee_id, starts_at, ends_at);

-- -----------------------------------------------------------------------------
-- 4. Services & pricing
-- -----------------------------------------------------------------------------

CREATE TABLE service_categories (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(150) NOT NULL,
  description     TEXT,
  display_order   INT NOT NULL DEFAULT 0,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_service_category_name UNIQUE (tenant_id, name)
);

CREATE INDEX idx_service_categories_tenant ON service_categories(tenant_id);

CREATE TABLE services (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  category_id     UUID REFERENCES service_categories(id) ON DELETE SET NULL,
  code            VARCHAR(50) NOT NULL,
  name            VARCHAR(200) NOT NULL,
  description     TEXT,
  duration_minutes INT NOT NULL DEFAULT 60,
  icon            VARCHAR(20),
  image_url       TEXT,
  image_storage_key VARCHAR(500),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  is_bookable_online BOOLEAN NOT NULL DEFAULT TRUE,
  display_order   INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_service_code UNIQUE (tenant_id, code),
  CONSTRAINT chk_service_duration CHECK (duration_minutes > 0)
);

CREATE INDEX idx_services_tenant ON services(tenant_id);
CREATE INDEX idx_services_category ON services(category_id);

CREATE TABLE pet_sizes (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(100) NOT NULL,
  code            VARCHAR(50) NOT NULL,
  min_weight_kg   NUMERIC(6, 2),
  max_weight_kg   NUMERIC(6, 2),
  display_order   INT NOT NULL DEFAULT 0,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_pet_size_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_pet_sizes_tenant ON pet_sizes(tenant_id);

CREATE TABLE service_prices (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  service_id      UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
  pet_size_id     UUID NOT NULL REFERENCES pet_sizes(id) ON DELETE CASCADE,
  price_cents     INT NOT NULL,
  currency        CHAR(3) NOT NULL DEFAULT 'RSD',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_service_pet_size_price UNIQUE (service_id, pet_size_id),
  CONSTRAINT chk_price_non_negative CHECK (price_cents >= 0)
);

CREATE INDEX idx_service_prices_tenant ON service_prices(tenant_id);
CREATE INDEX idx_service_prices_service ON service_prices(service_id);

CREATE TABLE employee_services (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  service_id      UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_employee_service UNIQUE (employee_id, service_id)
);

CREATE INDEX idx_employee_services_tenant ON employee_services(tenant_id);
CREATE INDEX idx_employee_services_employee ON employee_services(employee_id);
CREATE INDEX idx_employee_services_service ON employee_services(service_id);

-- -----------------------------------------------------------------------------
-- 5. Customers & pets
-- -----------------------------------------------------------------------------

CREATE TABLE customers (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  email           VARCHAR(255),
  phone           VARCHAR(50),
  notes           TEXT,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_tenant ON customers(tenant_id);
CREATE INDEX idx_customers_email ON customers(tenant_id, email);

CREATE TABLE pets (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  customer_id     UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  pet_size_id     UUID REFERENCES pet_sizes(id) ON DELETE SET NULL,
  name            VARCHAR(100) NOT NULL,
  species         VARCHAR(50) NOT NULL DEFAULT 'DOG',
  breed           VARCHAR(100),
  gender          pet_gender NOT NULL DEFAULT 'UNKNOWN',
  date_of_birth   DATE,
  weight_kg       NUMERIC(6, 2),
  color           VARCHAR(100),
  notes           TEXT,
  photo_url       TEXT,
  photo_storage_key VARCHAR(500),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pets_tenant ON pets(tenant_id);
CREATE INDEX idx_pets_customer ON pets(customer_id);

-- -----------------------------------------------------------------------------
-- 6. Appointments
-- -----------------------------------------------------------------------------

CREATE TABLE appointments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  location_id     UUID REFERENCES locations(id) ON DELETE SET NULL,
  customer_id     UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
  pet_id          UUID NOT NULL REFERENCES pets(id) ON DELETE RESTRICT,
  employee_id     UUID REFERENCES employees(id) ON DELETE SET NULL,
  status          appointment_status NOT NULL DEFAULT 'PENDING',
  starts_at       TIMESTAMPTZ NOT NULL,
  ends_at         TIMESTAMPTZ NOT NULL,
  total_price_cents INT,
  currency        CHAR(3) NOT NULL DEFAULT 'RSD',
  notes           TEXT,
  cancellation_reason TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_appointment_range CHECK (starts_at < ends_at)
);

CREATE INDEX idx_appointments_tenant ON appointments(tenant_id);
CREATE INDEX idx_appointments_customer ON appointments(customer_id);
CREATE INDEX idx_appointments_pet ON appointments(pet_id);
CREATE INDEX idx_appointments_employee ON appointments(employee_id);
CREATE INDEX idx_appointments_starts ON appointments(tenant_id, starts_at);
CREATE INDEX idx_appointments_status ON appointments(tenant_id, status);

CREATE TABLE appointment_services (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  appointment_id  UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
  service_id      UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
  pet_size_id     UUID REFERENCES pet_sizes(id) ON DELETE SET NULL,
  price_cents     INT NOT NULL,
  duration_minutes INT NOT NULL,
  display_order   INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_appt_service_price CHECK (price_cents >= 0),
  CONSTRAINT chk_appt_service_duration CHECK (duration_minutes > 0)
);

CREATE INDEX idx_appointment_services_tenant ON appointment_services(tenant_id);
CREATE INDEX idx_appointment_services_appointment ON appointment_services(appointment_id);

-- -----------------------------------------------------------------------------
-- 7. Website / front-end content
-- -----------------------------------------------------------------------------

CREATE TABLE salon_gallery (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  title           VARCHAR(255),
  caption         TEXT,
  media_type      media_type NOT NULL DEFAULT 'IMAGE',
  media_url       TEXT NOT NULL,
  storage_key     VARCHAR(500) NOT NULL,
  thumbnail_url   TEXT,
  alt_text        VARCHAR(255),
  display_order   INT NOT NULL DEFAULT 0,
  is_published    BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_salon_gallery_tenant ON salon_gallery(tenant_id);

CREATE TABLE salon_social_links (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  platform        VARCHAR(50) NOT NULL,
  url             VARCHAR(500) NOT NULL,
  handle          VARCHAR(100),
  display_order   INT NOT NULL DEFAULT 0,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_salon_social_platform UNIQUE (tenant_id, platform)
);

CREATE INDEX idx_salon_social_links_tenant ON salon_social_links(tenant_id);

CREATE TABLE testimonials (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  author_name     VARCHAR(150) NOT NULL,
  author_role     VARCHAR(100),
  content         TEXT NOT NULL,
  rating          SMALLINT,
  avatar_url      TEXT,
  avatar_storage_key VARCHAR(500),
  is_published    BOOLEAN NOT NULL DEFAULT FALSE,
  display_order   INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_testimonial_rating CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5))
);

CREATE INDEX idx_testimonials_tenant ON testimonials(tenant_id);

CREATE TABLE site_sections (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  section_key     VARCHAR(100) NOT NULL,
  title           VARCHAR(255),
  subtitle        VARCHAR(500),
  body            TEXT,
  content_json    JSONB NOT NULL DEFAULT '{}'::jsonb,
  image_url       TEXT,
  image_storage_key VARCHAR(500),
  cta_label       VARCHAR(100),
  cta_url         VARCHAR(500),
  is_visible      BOOLEAN NOT NULL DEFAULT TRUE,
  display_order   INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_site_section_key UNIQUE (tenant_id, section_key)
);

CREATE INDEX idx_site_sections_tenant ON site_sections(tenant_id);

CREATE TABLE tenant_features (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  feature_key     VARCHAR(100) NOT NULL,
  is_enabled      BOOLEAN NOT NULL DEFAULT FALSE,
  config_json     JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_tenant_feature UNIQUE (tenant_id, feature_key)
);

CREATE INDEX idx_tenant_features_tenant ON tenant_features(tenant_id);

-- -----------------------------------------------------------------------------
-- updated_at trigger
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
  t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'tenants', 'salon_profile', 'salon_branding', 'locations', 'location_working_hours',
    'users', 'tenant_users', 'employees', 'employee_working_hours', 'employee_time_blocks',
    'service_categories', 'services', 'pet_sizes', 'service_prices',
    'customers', 'pets', 'appointments',
    'salon_gallery', 'salon_social_links', 'testimonials', 'site_sections', 'tenant_features'
  ]
  LOOP
    EXECUTE format(
      'CREATE TRIGGER trg_%s_updated_at BEFORE UPDATE ON %I
       FOR EACH ROW EXECUTE FUNCTION set_updated_at()',
      t, t
    );
  END LOOP;
END $$;

-- -----------------------------------------------------------------------------
-- Row Level Security
-- App must: SET app.current_tenant_id = '<uuid>';
-- Super-admin / migrations: SET app.bypass_rls = 'true';
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION current_tenant_id()
RETURNS UUID AS $$
BEGIN
  RETURN NULLIF(current_setting('app.current_tenant_id', TRUE), '')::UUID;
EXCEPTION
  WHEN others THEN
    RETURN NULL;
END;
$$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION bypass_rls()
RETURNS BOOLEAN AS $$
BEGIN
  RETURN COALESCE(current_setting('app.bypass_rls', TRUE), 'false') = 'true';
EXCEPTION
  WHEN others THEN
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql STABLE;

DO $$
DECLARE
  tenant_table TEXT;
BEGIN
  FOREACH tenant_table IN ARRAY ARRAY[
    'salon_profile', 'salon_branding', 'locations', 'location_working_hours',
    'tenant_users', 'employees', 'employee_services', 'employee_working_hours',
    'employee_time_blocks', 'service_categories', 'services', 'pet_sizes',
    'service_prices', 'customers', 'pets', 'appointments', 'appointment_services',
    'salon_gallery', 'salon_social_links', 'testimonials', 'site_sections',
    'tenant_features'
  ]
  LOOP
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', tenant_table);
    EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', tenant_table);

    EXECUTE format(
      'CREATE POLICY tenant_isolation_select ON %I
       FOR SELECT USING (bypass_rls() OR tenant_id = current_tenant_id())',
      tenant_table
    );
    EXECUTE format(
      'CREATE POLICY tenant_isolation_insert ON %I
       FOR INSERT WITH CHECK (bypass_rls() OR tenant_id = current_tenant_id())',
      tenant_table
    );
    EXECUTE format(
      'CREATE POLICY tenant_isolation_update ON %I
       FOR UPDATE USING (bypass_rls() OR tenant_id = current_tenant_id())
       WITH CHECK (bypass_rls() OR tenant_id = current_tenant_id())',
      tenant_table
    );
    EXECUTE format(
      'CREATE POLICY tenant_isolation_delete ON %I
       FOR DELETE USING (bypass_rls() OR tenant_id = current_tenant_id())',
      tenant_table
    );
  END LOOP;
END $$;

ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenants FORCE ROW LEVEL SECURITY;

CREATE POLICY tenants_select ON tenants
  FOR SELECT USING (bypass_rls() OR id = current_tenant_id());

CREATE POLICY tenants_insert ON tenants
  FOR INSERT WITH CHECK (bypass_rls());

CREATE POLICY tenants_update ON tenants
  FOR UPDATE USING (bypass_rls() OR id = current_tenant_id())
  WITH CHECK (bypass_rls() OR id = current_tenant_id());

CREATE POLICY tenants_delete ON tenants
  FOR DELETE USING (bypass_rls());

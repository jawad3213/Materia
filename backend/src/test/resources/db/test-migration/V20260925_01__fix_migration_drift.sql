-- ===================================================================
-- Brings the migration history back in line with the JPA entity mappings.
--
-- These columns are mapped in the entities, and Hibernate creates them in every
-- deployed environment via ddl-auto, but no migration ever added them. The drift
-- went unnoticed because these migrations had never run anywhere. The test
-- profile's ddl-auto=validate is what exposed it (FINDING-010).
--
-- Test-only for now. When production is moved onto Flyway, this migration must
-- come with it, or startup validation will fail there too.
-- ===================================================================

-- users.must_change_password: set true when an account is provisioned, so the user
-- must choose their own password at first sign-in. NOT NULL with a default, so any
-- existing rows are backfilled safely.
alter table if exists public.users
    add column if not exists must_change_password boolean default false not null;

-- refresh_tokens audit columns, present on every other audited table.
alter table if exists public.refresh_tokens
    add column if not exists created_by varchar(100);

alter table if exists public.refresh_tokens
    add column if not exists updated_by varchar(100);

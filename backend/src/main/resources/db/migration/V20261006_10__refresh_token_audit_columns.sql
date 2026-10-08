-- refresh_tokens audit columns, mapped by the entity like on every other audited table. Hibernate added them on
-- developer databases (ddl-auto) but no production migration did, so a fresh install failed validation.
alter table if exists public.refresh_tokens add column if not exists created_by varchar(100);
alter table if exists public.refresh_tokens add column if not exists updated_by varchar(100);

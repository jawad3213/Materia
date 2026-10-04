-- Migration to drop username column if it already exists from prior schema
alter table if exists public.users drop column if exists username;
alter table if exists public.users alter column email set not null;

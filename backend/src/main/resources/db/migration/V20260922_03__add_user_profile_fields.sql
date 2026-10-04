-- Migration to add user profile, status, and department fields
alter table if exists public.users add column if not exists first_name varchar(100);
alter table if exists public.users add column if not exists last_name varchar(100);
alter table if exists public.users add column if not exists full_name varchar(200);
alter table if exists public.users add column if not exists phone varchar(30);
alter table if exists public.users add column if not exists status varchar(30) not null default 'ACTIVE';
alter table if exists public.users add column if not exists department varchar(100);

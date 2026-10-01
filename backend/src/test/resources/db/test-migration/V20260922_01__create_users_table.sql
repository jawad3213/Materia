create table if not exists public.users (
    id uuid not null primary key,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp default current_timestamp,
    version bigint default 0,
    created_by varchar(100),
    updated_by varchar(100),
    email varchar(255) not null,
    password_hash varchar(255) not null,
    role varchar(50) not null,
    enabled boolean not null default true,
    constraint uk_users_email unique (email)
);

create index if not exists idx_users_email on public.users(email);

-- Seed initial accounts for the 3 MVP roles: ADMIN, PURCHASER, RECEIVER
insert into public.users (id, created_at, updated_at, version, created_by, updated_by, email, password_hash, role, enabled)
values 
    ('a0000000-0000-0000-0000-000000000001', current_timestamp, current_timestamp, 0, 'system', 'system', 'admin@materia.com', '$2b$12$ShbjFpng4yhFyedTO7wBE.qT/IijNHEHUN9J2gBSKt7SUKKCKhMUO', 'ADMIN', true),
    ('a0000000-0000-0000-0000-000000000002', current_timestamp, current_timestamp, 0, 'system', 'system', 'purchaser@materia.com', '$2b$12$CRoxcZuKu2OJMIIEafNt.OK/VuXvc27G69IcEpGonCxlZ7iJDgFQ.', 'PURCHASER', true),
    ('a0000000-0000-0000-0000-000000000003', current_timestamp, current_timestamp, 0, 'system', 'system', 'receiver@materia.com', '$2b$12$CRoxcZuKu2OJMIIEafNt.OK/VuXvc27G69IcEpGonCxlZ7iJDgFQ.', 'RECEIVER', true)
on conflict (email) do nothing;

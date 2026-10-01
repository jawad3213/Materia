-- Migration: Create employees table
create table if not exists public.employees (
    id uuid not null primary key,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp default current_timestamp,
    version bigint default 0,
    created_by varchar(100),
    updated_by varchar(100),
    code varchar(50) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    full_name varchar(200) not null,
    email varchar(255) not null,
    phone varchar(50),
    department varchar(100),
    job_title varchar(100),
    status varchar(50) not null default 'ACTIVE',
    user_id uuid,
    hire_date date,
    termination_date date,
    termination_reason varchar(500),
    constraint uk_employees_code unique (code),
    constraint uk_employees_email unique (email),
    constraint fk_employees_users foreign key (user_id) references public.users(id) on delete set null
);

create index if not exists idx_employees_code on public.employees(code);
create index if not exists idx_employees_email on public.employees(email);
create index if not exists idx_employees_department on public.employees(department);
create index if not exists idx_employees_status on public.employees(status);
create index if not exists idx_employees_user_id on public.employees(user_id);

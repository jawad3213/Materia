create table if not exists public.password_reset_tokens (
    id uuid not null primary key,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp default current_timestamp,
    version bigint default 0,
    created_by varchar(100),
    updated_by varchar(100),
    token varchar(255) not null,
    user_id uuid not null,
    email varchar(255) not null,
    expiry_date timestamp not null,
    used boolean not null default false,
    constraint uk_password_reset_tokens_token unique (token),
    constraint fk_password_reset_tokens_user foreign key (user_id) references public.users(id) on delete cascade
);

create index if not exists idx_prt_token on public.password_reset_tokens(token);
create index if not exists idx_prt_email on public.password_reset_tokens(email);
create index if not exists idx_prt_user_id on public.password_reset_tokens(user_id);

-- Migration: Create payment tables
--
-- Mirrors the schema Hibernate generates for PaymentJpaEntity and PaymentLineJpaEntity (the same
-- definition the test schema uses). Every statement is idempotent: environments that ran with
-- ddl-auto=update already have these tables.

create table if not exists public.payments (
    id uuid not null primary key,
    payment_code varchar(50) not null unique,
    status varchar(30) not null check (status in ('DRAFT','PENDING','COMPLETED','CANCELLED')),
    supplier_id varchar(100) not null,
    supplier_name varchar(255) not null,
    supplier_code varchar(50),
    total_amount numeric(19,4) not null,
    paid_amount numeric(19,4),
    currency_code varchar(3) not null,
    payment_date timestamp(6),
    confirmed_date timestamp(6),
    bank_reference varchar(100),
    transaction_id varchar(100),
    payment_method varchar(50),
    payment_receipt varchar(255),
    notes varchar(1000),
    internal_notes varchar(1000),
    created_at timestamp(6) not null,
    created_by varchar(100),
    updated_at timestamp(6),
    updated_by varchar(100),
    version bigint
);

create table if not exists public.payment_lines (
    id uuid not null primary key,
    payment_id uuid not null,
    line_number integer not null,
    invoice_id varchar(100) not null,
    invoice_code varchar(100),
    supplier_id varchar(100) not null,
    supplier_name varchar(255),
    amount numeric(19,4) not null,
    paid_amount numeric(19,4),
    currency_code varchar(3) not null,
    is_paid boolean not null,
    notes varchar(1000),
    created_at timestamp(6) not null,
    created_by varchar(100),
    updated_at timestamp(6),
    updated_by varchar(100),
    version bigint
);

do $$
begin
    if not exists (
        select 1
        from pg_constraint c
        join pg_class t on t.oid = c.conrelid
        join pg_class r on r.oid = c.confrelid
        join pg_namespace n on n.oid = t.relnamespace
        where c.contype = 'f'
          and n.nspname = 'public'
          and t.relname = 'payment_lines'
          and r.relname = 'payments'
    ) then
        alter table public.payment_lines
            add constraint fk_payment_lines_payment
            foreign key (payment_id) references public.payments (id);
    end if;
end $$;

create index if not exists idx_payment_status on public.payments (status);
create index if not exists idx_payment_supplier_id on public.payments (supplier_id);
create index if not exists idx_payment_lines_payment_id on public.payment_lines (payment_id);
create index if not exists idx_payment_lines_invoice_id on public.payment_lines (invoice_id);

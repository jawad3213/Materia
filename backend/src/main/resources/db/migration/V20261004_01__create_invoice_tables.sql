-- Migration: Create invoice tables
--
-- Mirrors the schema Hibernate generates for InvoiceJpaEntity and InvoiceLineJpaEntity (the same
-- definition the test schema uses). Every statement is idempotent: environments that ran with
-- ddl-auto=update already have these tables.

create table if not exists public.invoices (
    id uuid not null primary key,
    invoice_code varchar(50) not null unique,
    invoice_type varchar(30) not null check (invoice_type in ('STANDARD','CREDIT_NOTE')),
    status varchar(30) not null check (status in ('DRAFT','SUBMITTED','VERIFIED','PAID','CANCELLED')),
    purchase_order_id varchar(100),
    purchase_order_code varchar(100),
    goods_receipt_id varchar(100),
    goods_receipt_code varchar(100),
    supplier_id varchar(100) not null,
    supplier_name varchar(255) not null,
    supplier_code varchar(50),
    external_reference varchar(100),
    invoice_date date not null,
    due_date date,
    received_date date,
    payment_date date,
    currency_code varchar(3) not null,
    total_amount numeric(19,4) not null,
    total_tax_amount numeric(19,4) not null,
    total_amount_with_tax numeric(19,4) not null,
    is_verified boolean not null,
    has_discrepancy boolean not null,
    discrepancy_summary varchar(1000),
    verification_date timestamp(6),
    verified_by varchar(100),
    verified_by_name varchar(255),
    paid_amount numeric(19,4),
    paid_at timestamp(6),
    paid_by varchar(100),
    paid_by_name varchar(255),
    notes varchar(1000),
    internal_notes varchar(1000),
    created_at timestamp(6) not null,
    created_by varchar(100),
    updated_at timestamp(6),
    updated_by varchar(100),
    version bigint
);

create table if not exists public.invoice_lines (
    id uuid not null primary key,
    invoice_id uuid not null,
    line_number integer not null,
    purchase_order_line_id varchar(100),
    goods_receipt_line_id varchar(100),
    material_code varchar(50),
    material_name varchar(255) not null,
    unit_of_measure varchar(50),
    quantity_ordered integer,
    quantity_received integer,
    quantity_invoiced integer not null,
    quantity_discrepancy integer,
    unit_price numeric(19,4) not null,
    line_total numeric(19,4) not null,
    tax_amount numeric(19,4),
    line_total_with_tax numeric(19,4),
    currency_code varchar(3) not null,
    has_quantity_discrepancy boolean not null,
    discrepancy_notes varchar(1000),
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
          and t.relname = 'invoice_lines'
          and r.relname = 'invoices'
    ) then
        alter table public.invoice_lines
            add constraint fk_invoice_lines_invoice
            foreign key (invoice_id) references public.invoices (id);
    end if;
end $$;

create index if not exists idx_invoice_status on public.invoices (status);
create index if not exists idx_invoice_supplier_id on public.invoices (supplier_id);
create index if not exists idx_invoice_po_id on public.invoices (purchase_order_id);
create index if not exists idx_invoice_date on public.invoices (invoice_date);
create index if not exists idx_invoice_lines_invoice_id on public.invoice_lines (invoice_id);

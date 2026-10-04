-- Migration: Create purchase order tables
--
-- Mirrors the schema Hibernate generates for PurchaseOrderJpaEntity and PurchaseOrderLineJpaEntity.
-- Every statement is idempotent: environments that ran with ddl-auto=update already have these tables.

create table if not exists public.purchase_orders (
    id uuid not null primary key,
    created_at timestamp(6) not null,
    updated_at timestamp(6),
    version bigint,
    created_by varchar(100),
    updated_by varchar(100),
    order_code varchar(50) not null unique,
    requisition_id uuid,
    requisition_code varchar(100),
    status varchar(30) not null
        check (status in ('DRAFT','SUBMITTED','CONFIRMED','READY_FOR_RECEIPT','RECEIVED',
                          'PARTIALLY_RECEIVED','COMPLETED','CANCELLED','REJECTED')),
    delivery_status varchar(30) not null
        check (delivery_status in ('NOT_SHIPPED','SHIPPED','IN_TRANSIT','PARTIAL','DELIVERED','DELAYED')),
    supplier_id uuid not null,
    supplier_name varchar(255) not null,
    supplier_code varchar(50),
    order_date date,
    expected_delivery_date date,
    confirmed_delivery_date date,
    received_date date,
    payment_terms varchar(255),
    payment_delay_days integer,
    delivery_terms varchar(255),
    incoterm varchar(20),
    currency_code varchar(10),
    total_amount numeric(19,4),
    tax_amount numeric(19,4),
    shipping_cost numeric(19,4),
    grand_total numeric(19,4),
    ordered_by varchar(100),
    ordered_by_name varchar(255),
    approved_by varchar(100),
    approved_by_name varchar(255),
    assigned_to varchar(100),
    assigned_to_name varchar(255),
    assigned_at timestamp(6),
    assigned_by varchar(100),
    assigned_by_name varchar(255),
    notes varchar(1000),
    internal_notes varchar(1000),
    obsoleted_at timestamp(6),
    obsoleted_by varchar(100),
    obsoleted_reason varchar(1000)
);

create table if not exists public.purchase_order_lines (
    id uuid not null primary key,
    created_at timestamp(6) not null,
    updated_at timestamp(6),
    version bigint,
    created_by varchar(100),
    updated_by varchar(100),
    purchase_order_id uuid not null,
    line_number integer,
    requisition_line_id uuid,
    material_id uuid,
    material_code varchar(50),
    material_name varchar(255),
    material_description varchar(1000),
    unit_of_measure varchar(20),
    quantity integer,
    unit_price numeric(19,4),
    line_total numeric(19,4),
    currency_code varchar(10),
    supplier_id uuid,
    supplier_name varchar(255),
    expected_delivery_date date,
    notes varchar(1000)
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
          and t.relname = 'purchase_order_lines'
          and r.relname = 'purchase_orders'
    ) then
        alter table public.purchase_order_lines
            add constraint fk_pol_purchase_order
            foreign key (purchase_order_id) references public.purchase_orders (id);
    end if;
end $$;

create index if not exists idx_po_status on public.purchase_orders (status);
create index if not exists idx_po_delivery_status on public.purchase_orders (delivery_status);
create index if not exists idx_po_supplier_id on public.purchase_orders (supplier_id);
create index if not exists idx_po_requisition_id on public.purchase_orders (requisition_id);
create index if not exists idx_po_order_date on public.purchase_orders (order_date);
create index if not exists idx_po_expected_delivery_date on public.purchase_orders (expected_delivery_date);
create index if not exists idx_pol_purchase_order_id on public.purchase_order_lines (purchase_order_id);
create index if not exists idx_pol_material_code on public.purchase_order_lines (material_code);
create index if not exists idx_pol_line_number on public.purchase_order_lines (line_number);

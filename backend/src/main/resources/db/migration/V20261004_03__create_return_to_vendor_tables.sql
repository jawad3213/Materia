-- Migration: Create return-to-vendor tables
--
-- Mirrors ReturnToVendorJpaEntity and ReturnToVendorLineJpaEntity. Every statement is idempotent:
-- environments that ran with ddl-auto=update may already have the tables, so the chain columns added for
-- the goods-receipt / purchase-order link are added separately when missing.

create table if not exists public.return_to_vendor (
    id uuid not null primary key,
    return_code varchar(50) not null unique,
    goods_receipt_id varchar(100),
    goods_receipt_code varchar(100),
    purchase_order_id varchar(100),
    purchase_order_code varchar(100),
    supplier_id varchar(100),
    supplier_name varchar(200),
    supplier_code varchar(100),
    status varchar(50),
    resolution_type varchar(50),
    return_date date,
    resolution_date date,
    return_reason varchar(500),
    supplier_response varchar(500),
    rejection_summary varchar(500),
    credit_note_reference varchar(100),
    credit_note_amount varchar(50),
    replacement_purchase_order_reference varchar(100),
    replacement_purchase_order_code varchar(100),
    notes varchar(1000),
    internal_notes varchar(1000),
    created_at timestamp(6) not null,
    created_by varchar(100),
    updated_at timestamp(6),
    updated_by varchar(100),
    version bigint
);

create table if not exists public.return_to_vendor_lines (
    id uuid not null primary key,
    return_to_vendor_id uuid not null,
    line_number integer,
    goods_receipt_line_id varchar(100),
    material_code varchar(100),
    material_name varchar(200),
    unit_of_measure varchar(50),
    rejected_quantity integer,
    quantity_to_return integer,
    quantity_already_returned integer,
    rejection_reason varchar(500),
    quality_notes varchar(500),
    defect_description varchar(500),
    is_replaced boolean,
    is_credit_note boolean,
    notes varchar(500),
    created_at timestamp(6) not null,
    created_by varchar(100),
    updated_at timestamp(6),
    updated_by varchar(100),
    version bigint
);

alter table public.return_to_vendor add column if not exists currency_code varchar(3);
alter table public.return_to_vendor_lines add column if not exists purchase_order_line_id varchar(100);
alter table public.return_to_vendor_lines add column if not exists material_id varchar(100);
alter table public.return_to_vendor_lines add column if not exists unit_price numeric(19,4);

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
          and t.relname = 'return_to_vendor_lines'
          and r.relname = 'return_to_vendor'
    ) then
        alter table public.return_to_vendor_lines
            add constraint fk_return_to_vendor_lines_return
            foreign key (return_to_vendor_id) references public.return_to_vendor (id);
    end if;
end $$;

create index if not exists idx_return_to_vendor_status on public.return_to_vendor (status);
create index if not exists idx_return_to_vendor_goods_receipt on public.return_to_vendor (goods_receipt_id);
create index if not exists idx_return_to_vendor_purchase_order on public.return_to_vendor (purchase_order_id);
create index if not exists idx_return_to_vendor_supplier on public.return_to_vendor (supplier_id);
create index if not exists idx_return_to_vendor_lines_return on public.return_to_vendor_lines (return_to_vendor_id);

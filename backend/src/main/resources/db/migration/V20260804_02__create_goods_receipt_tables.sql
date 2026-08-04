create table if not exists public.goods_receipts (
    id uuid not null primary key,
    created_at timestamp not null,
    updated_at timestamp,
    version bigint,
    created_by varchar(100),
    updated_by varchar(100),
    receipt_code varchar(50) not null,
    purchase_order_id varchar(100) not null,
    purchase_order_code varchar(100),
    status varchar(30) not null,
    receipt_date date,
    expected_delivery_date date,
    received_by varchar(100) not null,
    received_by_name varchar(255) not null,
    notes varchar(1000),
    supplier_id varchar(100),
    supplier_name varchar(255),
    total_quantity_ordered integer,
    total_quantity_received integer,
    total_quantity_rejected integer,
    total_quantity_accepted integer,
    has_discrepancy boolean not null default false,
    discrepancy_notes varchar(1000),
    obsoleted_at timestamp,
    obsoleted_by varchar(100),
    obsoleted_reason varchar(1000),
    constraint uk_goods_receipts_receipt_code unique (receipt_code)
);

create table if not exists public.goods_receipt_lines (
    id uuid not null primary key,
    created_at timestamp not null,
    updated_at timestamp,
    version bigint,
    created_by varchar(100),
    updated_by varchar(100),
    goods_receipt_id uuid not null,
    line_number integer,
    purchase_order_line_id varchar(100),
    material_code varchar(50) not null,
    material_id uuid,
    material_name varchar(255),
    unit_of_measure varchar(20),
    quantity_ordered integer,
    quantity_received integer,
    quantity_rejected integer,
    quantity_accepted integer,
    quantity_pending integer,
    quality_status varchar(30),
    quality_notes varchar(1000),
    rejection_reason varchar(1000),
    stock_before integer,
    stock_after integer,
    unit_price numeric(19,4),
    line_total numeric(19,4),
    currency_code varchar(10),
    supplier_id varchar(100),
    supplier_name varchar(255),
    batch_number varchar(100),
    expiry_date date,
    storage_location varchar(100),
    notes varchar(1000),
    constraint fk_goods_receipt_lines_receipt
        foreign key (goods_receipt_id) references public.goods_receipts(id) on delete cascade
);

create index if not exists idx_gr_receipt_code on public.goods_receipts (receipt_code);
create index if not exists idx_gr_purchase_order_id on public.goods_receipts (purchase_order_id);
create index if not exists idx_gr_status on public.goods_receipts (status);
create index if not exists idx_gr_received_by on public.goods_receipts (received_by);
create index if not exists idx_gr_supplier_id on public.goods_receipts (supplier_id);
create index if not exists idx_gr_receipt_date on public.goods_receipts (receipt_date);

create index if not exists idx_grl_goods_receipt_id on public.goods_receipt_lines (goods_receipt_id);
create index if not exists idx_grl_purchase_order_line_id on public.goods_receipt_lines (purchase_order_line_id);
create index if not exists idx_grl_material_code on public.goods_receipt_lines (material_code);
create index if not exists idx_grl_line_number on public.goods_receipt_lines (line_number);

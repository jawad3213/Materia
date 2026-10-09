-- Migration: Create the attachments table
--
-- Files attached to purchase orders, goods receipts, invoices and returns (supplier invoice scans, delivery
-- notes, quality photos). Mirrors AttachmentJpaEntity. The bytes are kept outside the database, under
-- app.attachments.storage-dir, at storage_key. owner_id points to a different table per owner_type, so it has
-- no foreign key; the application checks the document exists before attaching.

create table if not exists public.attachments (
    id uuid not null primary key,
    owner_type varchar(30) not null,
    owner_id uuid not null,
    category varchar(30) not null,
    file_name varchar(255) not null,
    content_type varchar(100) not null,
    size_bytes bigint not null,
    storage_key varchar(300) not null unique,
    uploaded_by varchar(100),
    uploaded_by_name varchar(200),
    uploaded_at timestamp(6) not null,
    constraint ck_attachments_owner_type check (owner_type in ('PURCHASE_ORDER', 'GOODS_RECEIPT', 'INVOICE', 'RETURN_TO_VENDOR')),
    constraint ck_attachments_category check (category in ('SUPPLIER_INVOICE', 'DELIVERY_NOTE', 'QUALITY_PHOTO', 'OTHER')),
    constraint ck_attachments_size check (size_bytes > 0)
);

create index if not exists idx_attachments_owner on public.attachments (owner_type, owner_id);

-- ===================================================================
-- Baseline schema for a fresh install.
--
-- Until now these tables were only ever created by Hibernate (ddl-auto) on developer machines, and the oldest
-- migrations below alter them, so a new database failed on boot. This migration creates them first, with the
-- structure Hibernate validates (the same DDL as the test migrations).
--
-- Every statement is idempotent: on a database where Hibernate already created the tables nothing changes. A
-- database whose history is already past this version skips it (Flyway does not run older pending versions).
-- No account is seeded here: the first administrator comes from APP_BOOTSTRAP_ADMIN_EMAIL / _PASSWORD.
-- ===================================================================

-- ---- from create users table ----
create table if not exists public.users ( id uuid not null primary key, created_at timestamp not null default current_timestamp, updated_at timestamp default current_timestamp, version bigint default 0, created_by varchar(100), updated_by varchar(100), email varchar(255) not null, password_hash varchar(255) not null, role varchar(50) not null, enabled boolean not null default true, constraint uk_users_email unique (email) );
create index if not exists idx_users_email on public.users(email);

-- ---- from fix migration drift ----
alter table if exists public.users add column if not exists must_change_password boolean default false not null;

-- ---- from create master data tables ----
create table if not exists public.global_code_sequences (next_val integer not null, prefix varchar(30) not null, primary key (prefix));
create table if not exists public.categories (level integer, created_at timestamp(6) not null, updated_at timestamp(6), version bigint, id uuid not null, status varchar(20) not null, category_type varchar(30) check (category_type in ('MATERIAL','PRODUCT','SERVICE','RAW_MATERIAL_CAT','COMPONENT_CAT','PACKAGING_CAT','SPARE_PART_CAT','CONSUMABLE_CAT','TOOL_CAT','CHEMICAL_CAT','ELECTRONIC_CAT','FAMILY','BRAND','DEPARTMENT','PROJECT','GEOGRAPHIC','SEASONAL')), code varchar(50) not null unique, parent_code varchar(50), created_by varchar(100), name varchar(100) not null, updated_by varchar(100), short_description varchar(200), description varchar(500), path varchar(500), parent_id varchar(255), primary key (id));
create table if not exists public.suppliers (payment_delay integer, created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10) check (currency_code in ('MAD','EUR','USD')), id uuid not null, contact_phone varchar(20), postal_code varchar(20), status varchar(20) not null, code varchar(50) not null unique, city varchar(100), contact_person varchar(100), country varchar(100), created_by varchar(100), updated_by varchar(100), address varchar(500), description varchar(500), contact_email varchar(255), name varchar(255) not null, primary key (id));
create table if not exists public.supplier_payment_terms (supplier_id uuid not null, payment_term varchar(255));
create table if not exists public.materials (available_stock integer, average_purchase_price numeric(19,4), cost_price numeric(19,4), current_stock integer, economic_order_quantity integer, stock_on_order integer, last_purchase_price numeric(19,4), maximum_stock integer, minimum_stock integer, reorder_point integer, safety_stock integer, standard_price numeric(19,4), created_at timestamp(6) not null, obsoleted_at timestamp(6), updated_at timestamp(6), version bigint, average_purchase_price_currency varchar(10) check (average_purchase_price_currency in ('MAD','EUR','USD')), cost_price_currency varchar(10) check (cost_price_currency in ('MAD','EUR','USD')), last_purchase_price_currency varchar(10) check (last_purchase_price_currency in ('MAD','EUR','USD')), standard_price_currency varchar(10) check (standard_price_currency in ('MAD','EUR','USD')), id uuid not null, unit_of_measure varchar(20) check (unit_of_measure in ('KG','G','T','MG','LB','OZ','L','ML','M3','DM3','CM3','GAL','FT3','M','CM','MM','KM','IN','FT','YD','M2','CM2','MM2','HA','ACRE','PCE','BOX','CART','PACK','SET','PAL','DRUM','ROLL','SHEET','REEL','HOUR','DAY','WEEK','MONTH','KWH','KW','HP','C','F','K','BAR','PSI','PA','ATM','NONE')), material_type varchar(30) not null check (material_type in ('RAW_MATERIAL','FINISHED_GOOD','COMPONENT','PACKAGING','SPARE_PART','CONSUMABLE','SERVICE','TOOL','CHEMICAL','ELECTRONIC')), status varchar(30) not null check (status in ('DRAFT','ACTIVE','INACTIVE','BLOCKED','OBSOLETE')), code varchar(50) not null unique, category_name varchar(100), created_by varchar(100), obsoleted_by varchar(100), updated_by varchar(100), short_description varchar(200), obsoleted_reason varchar(500), search_keywords varchar(500), description varchar(1000), alternative_name varchar(255), category_id varchar(255), name varchar(255) not null, supplier_id varchar(255), supplier_name varchar(255), primary key (id));
create table if not exists public.material_stock_movements (new_stock integer not null, previous_stock integer not null, quantity integer not null, created_at timestamp(6) not null, occurred_at timestamp(6) not null, updated_at timestamp(6), version bigint, id uuid not null, material_id uuid not null, movement_type varchar(30) not null check (movement_type in ('OPENING_BALANCE','RECEIPT','ISSUE','ADJUSTMENT')), created_by varchar(100), updated_by varchar(100), reason varchar(500), primary key (id));
create index if not exists idx_category_parent_id on public.categories (parent_id);
create index if not exists idx_category_status on public.categories (status);
create index if not exists idx_category_type on public.categories (category_type);
create index if not exists idx_supplier_status on public.suppliers (status);
create index if not exists idx_supplier_country on public.suppliers (country);
create index if not exists idx_supplier_city on public.suppliers (city);
create index if not exists idx_material_category_id on public.materials (category_id);
create index if not exists idx_material_supplier_id on public.materials (supplier_id);
create index if not exists idx_material_status on public.materials (status);
do $$ begin
    if not exists (select 1 from pg_constraint where conname = 'fk8kpayf27d2wsb1cu8i22d940t') then
        alter table public.supplier_payment_terms add constraint FK8kpayf27d2wsb1cu8i22d940t foreign key (supplier_id) references public.suppliers;
    end if;
end $$;
do $$ begin
    if not exists (select 1 from pg_constraint where conname = 'fkkihr1ny42uednqq2dgv6y79v') then
        alter table public.material_stock_movements add constraint FKkihr1ny42uednqq2dgv6y79v foreign key (material_id) references public.materials;
    end if;
end $$;

-- ---- from create requisition tables ----
create table if not exists public.purchase_requisitions (approved_date date, cancelled_date date, converted_date date, required_date date, submitted_date date, total_amount numeric(19,4), created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10), id uuid not null, status varchar(30) not null check (status in ('DRAFT','SUBMITTED','APPROVED','REJECTED','CANCELLED','CONVERTED')), requisition_code varchar(50) not null unique, approver_id varchar(100), created_by varchar(100), purchase_order_code varchar(100), purchase_order_id varchar(100), requester_id varchar(100) not null, updated_by varchar(100), justification varchar(500), approval_notes varchar(1000), cancellation_reason varchar(1000), description varchar(1000), rejection_reason varchar(1000), approver_name varchar(255), requester_name varchar(255) not null, title varchar(255) not null, primary key (id));
create table if not exists public.purchase_requisition_lines (expiry_date date, line_number integer, line_total numeric(19,4), quantity integer, quantity_received integer, quantity_rejected integer, required_date date, standard_price numeric(19,4), unit_price numeric(19,4), created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10), currency_code_line varchar(10), id uuid not null, material_id uuid, requisition_id uuid not null, supplier_id uuid, unit_of_measure varchar(20), material_code varchar(50), supplier_code varchar(50), batch_number varchar(100), created_by varchar(100), storage_location varchar(100), updated_by varchar(100), material_description varchar(1000), notes varchar(1000), delivery_terms varchar(255), material_name varchar(255), supplier_name varchar(255), primary key (id));
create index if not exists idx_pr_status on public.purchase_requisitions (status);
create index if not exists idx_pr_requester_id on public.purchase_requisitions (requester_id);
create index if not exists idx_pr_approver_id on public.purchase_requisitions (approver_id);
create index if not exists idx_pr_required_date on public.purchase_requisitions (required_date);
create index if not exists idx_pr_line_requisition_id on public.purchase_requisition_lines (requisition_id);
create index if not exists idx_pr_line_material_code on public.purchase_requisition_lines (material_code);
create index if not exists idx_pr_line_line_number on public.purchase_requisition_lines (line_number);
do $$ begin
    if not exists (select 1 from pg_constraint where conname = 'fk9qdinktyun2qy11gv52akx9cx') then
        alter table public.purchase_requisition_lines add constraint FK9qdinktyun2qy11gv52akx9cx foreign key (requisition_id) references public.purchase_requisitions;
    end if;
end $$;

-- ---- from create out of scope tables ----
create table if not exists public.goods_receipts (expected_delivery_date date, has_discrepancy boolean not null, receipt_date date, total_quantity_accepted integer, total_quantity_ordered integer, total_quantity_received integer, total_quantity_rejected integer, created_at timestamp(6) not null, obsoleted_at timestamp(6), updated_at timestamp(6), version bigint, id uuid not null, status varchar(30) not null check (status in ('DRAFT','IN_PROGRESS','COMPLETED','PARTIAL','CANCELLED')), receipt_code varchar(50) not null unique, created_by varchar(100), obsoleted_by varchar(100), purchase_order_code varchar(100), purchase_order_id varchar(100) not null, received_by varchar(100) not null, supplier_id varchar(100), updated_by varchar(100), discrepancy_notes varchar(1000), notes varchar(1000), obsoleted_reason varchar(1000), received_by_name varchar(255) not null, supplier_name varchar(255), primary key (id));
create table if not exists public.goods_receipt_lines (expiry_date date, line_number integer, line_total numeric(19,4), quantity_accepted integer, quantity_ordered integer, quantity_pending integer, quantity_received integer, quantity_rejected integer, stock_after integer, stock_before integer, unit_price numeric(19,4), created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10), goods_receipt_id uuid not null, id uuid not null, material_id uuid, unit_of_measure varchar(20), quality_status varchar(30) check (quality_status in ('ACCEPTED','REJECTED','UNDER_REVIEW','PARTIAL')), material_code varchar(50) not null, batch_number varchar(100), created_by varchar(100), purchase_order_line_id varchar(100), storage_location varchar(100), supplier_id varchar(100), updated_by varchar(100), notes varchar(1000), quality_notes varchar(1000), rejection_reason varchar(1000), material_name varchar(255), supplier_name varchar(255), primary key (id));
create index if not exists idx_gr_purchase_order_id on public.goods_receipts (purchase_order_id);
create index if not exists idx_gr_status on public.goods_receipts (status);
create index if not exists idx_gr_received_by on public.goods_receipts (received_by);
create index if not exists idx_gr_supplier_id on public.goods_receipts (supplier_id);
create index if not exists idx_gr_receipt_date on public.goods_receipts (receipt_date);
create index if not exists idx_grl_goods_receipt_id on public.goods_receipt_lines (goods_receipt_id);
create index if not exists idx_grl_purchase_order_line_id on public.goods_receipt_lines (purchase_order_line_id);
create index if not exists idx_grl_material_code on public.goods_receipt_lines (material_code);
create index if not exists idx_grl_line_number on public.goods_receipt_lines (line_number);
do $$ begin
    if not exists (select 1 from pg_constraint where conname = 'fk911tv4c70yqo1u58rdfa0pf5w') then
        alter table public.goods_receipt_lines add constraint FK911tv4c70yqo1u58rdfa0pf5w foreign key (goods_receipt_id) references public.goods_receipts;
    end if;
end $$;

-- ===================================================================
-- Master data: categories, suppliers, materials, stock movements, code sequences
--
-- Generated from Hibernate's own DDL for the current entity mappings, exported
-- offline through the application's EntityManagerFactory (T009). It mirrors the
-- schema ddl-auto creates in deployed environments, so tests run against the
-- same shape as production. Deliberately adds no constraints Hibernate does not
-- generate: a stricter test schema would let tests pass that production fails.
--
-- In scope: US3 (stock) and US5 (catalogue).
-- Test-only: see FINDING-003 in specs/001-backend-module-tests/research.md.
-- ===================================================================

-- Tables
create table public.global_code_sequences (next_val integer not null, prefix varchar(30) not null, primary key (prefix));
create table public.categories (level integer, created_at timestamp(6) not null, updated_at timestamp(6), version bigint, id uuid not null, status varchar(20) not null, category_type varchar(30) check (category_type in ('MATERIAL','PRODUCT','SERVICE','RAW_MATERIAL_CAT','COMPONENT_CAT','PACKAGING_CAT','SPARE_PART_CAT','CONSUMABLE_CAT','TOOL_CAT','CHEMICAL_CAT','ELECTRONIC_CAT','FAMILY','BRAND','DEPARTMENT','PROJECT','GEOGRAPHIC','SEASONAL')), code varchar(50) not null unique, parent_code varchar(50), created_by varchar(100), name varchar(100) not null, updated_by varchar(100), short_description varchar(200), description varchar(500), path varchar(500), parent_id varchar(255), primary key (id));
create table public.suppliers (payment_delay integer, created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10) check (currency_code in ('MAD','EUR','USD')), id uuid not null, contact_phone varchar(20), postal_code varchar(20), status varchar(20) not null, code varchar(50) not null unique, city varchar(100), contact_person varchar(100), country varchar(100), created_by varchar(100), updated_by varchar(100), address varchar(500), description varchar(500), contact_email varchar(255), name varchar(255) not null, primary key (id));
create table public.supplier_payment_terms (supplier_id uuid not null, payment_term varchar(255));
create table public.materials (available_stock integer, average_purchase_price numeric(19,4), cost_price numeric(19,4), current_stock integer, economic_order_quantity integer, last_purchase_price numeric(19,4), maximum_stock integer, minimum_stock integer, reorder_point integer, safety_stock integer, standard_price numeric(19,4), created_at timestamp(6) not null, obsoleted_at timestamp(6), updated_at timestamp(6), version bigint, average_purchase_price_currency varchar(10) check (average_purchase_price_currency in ('MAD','EUR','USD')), cost_price_currency varchar(10) check (cost_price_currency in ('MAD','EUR','USD')), last_purchase_price_currency varchar(10) check (last_purchase_price_currency in ('MAD','EUR','USD')), standard_price_currency varchar(10) check (standard_price_currency in ('MAD','EUR','USD')), id uuid not null, unit_of_measure varchar(20) check (unit_of_measure in ('KG','G','T','MG','LB','OZ','L','ML','M3','DM3','CM3','GAL','FT3','M','CM','MM','KM','IN','FT','YD','M2','CM2','MM2','HA','ACRE','PCE','BOX','CART','PACK','SET','PAL','DRUM','ROLL','SHEET','REEL','HOUR','DAY','WEEK','MONTH','KWH','KW','HP','C','F','K','BAR','PSI','PA','ATM','NONE')), material_type varchar(30) not null check (material_type in ('RAW_MATERIAL','FINISHED_GOOD','COMPONENT','PACKAGING','SPARE_PART','CONSUMABLE','SERVICE','TOOL','CHEMICAL','ELECTRONIC')), status varchar(30) not null check (status in ('DRAFT','ACTIVE','INACTIVE','BLOCKED','OBSOLETE')), code varchar(50) not null unique, category_name varchar(100), created_by varchar(100), obsoleted_by varchar(100), updated_by varchar(100), short_description varchar(200), obsoleted_reason varchar(500), search_keywords varchar(500), description varchar(1000), alternative_name varchar(255), category_id varchar(255), name varchar(255) not null, supplier_id varchar(255), supplier_name varchar(255), primary key (id));
create table public.material_stock_movements (new_stock integer not null, previous_stock integer not null, quantity integer not null, created_at timestamp(6) not null, occurred_at timestamp(6) not null, updated_at timestamp(6), version bigint, id uuid not null, material_id uuid not null, movement_type varchar(30) not null check (movement_type in ('OPENING_BALANCE','RECEIPT','ISSUE','ADJUSTMENT')), created_by varchar(100), updated_by varchar(100), reason varchar(500), primary key (id));

-- Indexes
create index idx_category_parent_id on public.categories (parent_id);
create index idx_category_status on public.categories (status);
create index idx_category_type on public.categories (category_type);
create index idx_supplier_status on public.suppliers (status);
create index idx_supplier_country on public.suppliers (country);
create index idx_supplier_city on public.suppliers (city);
create index idx_material_category_id on public.materials (category_id);
create index idx_material_supplier_id on public.materials (supplier_id);
create index idx_material_status on public.materials (status);

-- Foreign keys
alter table if exists public.supplier_payment_terms add constraint FK8kpayf27d2wsb1cu8i22d940t foreign key (supplier_id) references public.suppliers;
alter table if exists public.material_stock_movements add constraint FKkihr1ny42uednqq2dgv6y79v foreign key (material_id) references public.materials;

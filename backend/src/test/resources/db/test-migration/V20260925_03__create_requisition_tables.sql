-- ===================================================================
-- Purchase requisitions and their lines
--
-- Generated from Hibernate's own DDL for the current entity mappings, exported
-- offline through the application's EntityManagerFactory (T009). It mirrors the
-- schema ddl-auto creates in deployed environments, so tests run against the
-- same shape as production. Deliberately adds no constraints Hibernate does not
-- generate: a stricter test schema would let tests pass that production fails.
--
-- In scope: US2 (requisition workflow).
-- Test-only: see FINDING-003 in specs/001-backend-module-tests/research.md.
-- ===================================================================

-- Tables
create table public.purchase_requisitions (approved_date date, cancelled_date date, converted_date date, required_date date, submitted_date date, total_amount numeric(19,4), created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10), id uuid not null, status varchar(30) not null check (status in ('DRAFT','SUBMITTED','APPROVED','REJECTED','CANCELLED','CONVERTED')), requisition_code varchar(50) not null unique, approver_id varchar(100), created_by varchar(100), purchase_order_code varchar(100), purchase_order_id varchar(100), requester_id varchar(100) not null, updated_by varchar(100), justification varchar(500), approval_notes varchar(1000), cancellation_reason varchar(1000), description varchar(1000), rejection_reason varchar(1000), approver_name varchar(255), requester_name varchar(255) not null, title varchar(255) not null, primary key (id));
create table public.purchase_requisition_lines (expiry_date date, line_number integer, line_total numeric(19,4), quantity integer, quantity_received integer, quantity_rejected integer, required_date date, standard_price numeric(19,4), unit_price numeric(19,4), created_at timestamp(6) not null, updated_at timestamp(6), version bigint, currency_code varchar(10), currency_code_line varchar(10), id uuid not null, material_id uuid, requisition_id uuid not null, supplier_id uuid, unit_of_measure varchar(20), material_code varchar(50), supplier_code varchar(50), batch_number varchar(100), created_by varchar(100), storage_location varchar(100), updated_by varchar(100), material_description varchar(1000), notes varchar(1000), delivery_terms varchar(255), material_name varchar(255), supplier_name varchar(255), primary key (id));

-- Indexes
create index idx_pr_status on public.purchase_requisitions (status);
create index idx_pr_requester_id on public.purchase_requisitions (requester_id);
create index idx_pr_approver_id on public.purchase_requisitions (approver_id);
create index idx_pr_required_date on public.purchase_requisitions (required_date);
create index idx_pr_line_requisition_id on public.purchase_requisition_lines (requisition_id);
create index idx_pr_line_material_code on public.purchase_requisition_lines (material_code);
create index idx_pr_line_line_number on public.purchase_requisition_lines (line_number);

-- Foreign keys
alter table if exists public.purchase_requisition_lines add constraint FK9qdinktyun2qy11gv52akx9cx foreign key (requisition_id) references public.purchase_requisitions;

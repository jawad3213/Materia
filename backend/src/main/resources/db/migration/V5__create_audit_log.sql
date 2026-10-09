-- Change history of the business records: who changed what, when, and why.
-- One row per created, changed or deleted record. A line of a document (an order line, an invoice line...) also
-- names its document in parent_type / parent_id, so a document's history includes its lines.
-- changes holds, per field, the value before and after: {"status": {"from": "DRAFT", "to": "SUBMITTED"}}.
create table if not exists public.audit_log (
    id          uuid          not null primary key,
    occurred_at timestamp(6)  not null,
    actor_id    varchar(100)  not null,
    entity_type varchar(60)   not null,
    entity_id   uuid          not null,
    parent_type varchar(60),
    parent_id   uuid,
    action      varchar(20)   not null check (action in ('CREATE', 'UPDATE', 'STATUS_CHANGE', 'DELETE')),
    summary     varchar(500),
    reason      varchar(1000),
    changes     jsonb         not null default '{}'::jsonb
);
create index if not exists idx_audit_log_entity on public.audit_log (entity_type, entity_id, occurred_at desc);
create index if not exists idx_audit_log_parent on public.audit_log (parent_type, parent_id, occurred_at desc);
create index if not exists idx_audit_log_occurred on public.audit_log (occurred_at desc);
create index if not exists idx_audit_log_actor on public.audit_log (actor_id, occurred_at desc);

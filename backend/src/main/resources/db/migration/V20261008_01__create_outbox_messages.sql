-- Migration: Create the transactional outbox
--
-- Every domain event is written here in the transaction of the change that raised it (OutboxRecorder), then
-- relayed to Kafka by OutboxRelay. It is the hand-off point for the services the contexts will become.
-- Idempotent: environments that ran with ddl-auto=update may already have the table.

create table if not exists public.outbox_messages (
    id uuid not null primary key,
    event_id uuid not null unique,
    event_type varchar(150) not null,
    source_context varchar(60) not null,
    aggregate_id varchar(100),
    payload text not null,
    occurred_at timestamp(6) not null,
    created_at timestamp(6) not null,
    published_at timestamp(6),
    attempts integer not null,
    last_error varchar(1000)
);

-- The relay reads the oldest unpublished messages.
create index if not exists idx_outbox_messages_pending on public.outbox_messages (created_at) where published_at is null;

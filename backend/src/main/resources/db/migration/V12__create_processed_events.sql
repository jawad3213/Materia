-- Inbox of the event consumers: an event delivered more than once (the outbox relay delivers at least once, and the
-- same event can also arrive in-process and through Kafka) is applied only by its first delivery. A consumer records
-- the event id in the same transaction as its effect.
create table if not exists public.processed_events (
    consumer     varchar(100) not null,
    event_id     uuid         not null,
    event_type   varchar(100),
    processed_at timestamp(6) not null default now(),
    constraint pk_processed_events primary key (consumer, event_id)
);
create index if not exists idx_processed_events_processed_at on public.processed_events (processed_at);

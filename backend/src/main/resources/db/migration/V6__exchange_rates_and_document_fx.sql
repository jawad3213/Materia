-- Reporting currency (app.reporting-currency, MAD by default).
--
-- exchange_rates: one rate per day and currency pair, stored by a daily job from the exchange-rate service, so
-- documents can be valued at the rate of their own date and the dashboard does not call the external API.
-- The rate converts an amount in from_currency into to_currency (amount x rate).
create table if not exists public.exchange_rates (
    id            uuid          not null primary key,
    rate_date     date          not null,
    from_currency varchar(3)    not null,
    to_currency   varchar(3)    not null,
    rate          numeric(19,8) not null check (rate > 0),
    source        varchar(20)   not null,
    created_at    timestamp(6)  not null default now(),
    constraint uk_exchange_rates_day_pair unique (rate_date, from_currency, to_currency)
);
create index if not exists idx_exchange_rates_pair_date
    on public.exchange_rates (from_currency, to_currency, rate_date desc);

-- Each committed document keeps the rate it was valued at: order confirmed, invoice verified, payment completed.
-- fx_reporting_currency says which reporting currency the copy refers to; a copy made for another reporting currency
-- is ignored and the document is valued again.
alter table public.purchase_orders add column if not exists fx_rate_to_reporting numeric(19,8);
alter table public.purchase_orders add column if not exists amount_reporting numeric(19,4);
alter table public.purchase_orders add column if not exists fx_rate_date date;
alter table public.purchase_orders add column if not exists fx_rate_source varchar(20);
alter table public.purchase_orders add column if not exists fx_reporting_currency varchar(3);

alter table public.invoices add column if not exists fx_rate_to_reporting numeric(19,8);
alter table public.invoices add column if not exists amount_reporting numeric(19,4);
alter table public.invoices add column if not exists fx_rate_date date;
alter table public.invoices add column if not exists fx_rate_source varchar(20);
alter table public.invoices add column if not exists fx_reporting_currency varchar(3);

alter table public.payments add column if not exists fx_rate_to_reporting numeric(19,8);
alter table public.payments add column if not exists amount_reporting numeric(19,4);
alter table public.payments add column if not exists fx_rate_date date;
alter table public.payments add column if not exists fx_rate_source varchar(20);
alter table public.payments add column if not exists fx_reporting_currency varchar(3);

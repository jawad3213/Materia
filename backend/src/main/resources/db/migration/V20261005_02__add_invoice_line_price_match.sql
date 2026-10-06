-- Price match of each invoice line against its purchase-order line: the order price, the variance in percent and
-- whether it is beyond the tolerance (app.invoice.price-tolerance-percent). A line beyond it blocks verification.
alter table public.invoice_lines add column if not exists order_unit_price numeric(19,4);
alter table public.invoice_lines add column if not exists price_variance_percent numeric(9,2);
alter table public.invoice_lines add column if not exists has_price_discrepancy boolean not null default false;

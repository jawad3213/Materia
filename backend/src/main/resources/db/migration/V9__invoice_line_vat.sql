-- VAT per invoice line: the rate invoiced, the rate of the order line it was matched against, and whether its VAT
-- differs from what the order rate gives (which blocks verification). Existing lines get the effective rate of the
-- tax they were invoiced with; the order rate is recorded the next time the invoice is matched.
alter table public.invoice_lines add column if not exists tax_rate numeric(5,2);
alter table public.invoice_lines add column if not exists order_tax_rate numeric(5,2);
alter table public.invoice_lines add column if not exists has_tax_discrepancy boolean not null default false;

update public.invoice_lines
   set tax_rate = least(greatest(round(coalesce(tax_amount, 0) / line_total * 100, 2), 0), 100)
 where tax_rate is null and line_total > 0;

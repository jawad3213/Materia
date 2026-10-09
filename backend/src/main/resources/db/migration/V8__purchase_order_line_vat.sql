-- VAT per order line: each line has a VAT rate (percent) and the VAT it gives, and the order's VAT is their sum.
-- Existing orders carried one tax amount for all their lines: their lines get the order's effective rate
-- (tax / net total), so their VAT is unchanged; an order without a tax amount keeps lines without a rate.
alter table public.purchase_order_lines add column if not exists tax_rate numeric(5,2);
alter table public.purchase_order_lines add column if not exists tax_amount numeric(19,4);

update public.purchase_order_lines l
   set tax_rate   = r.rate,
       tax_amount = round(coalesce(l.line_total, 0) * r.rate / 100, 2)
  from (select id, least(greatest(round(tax_amount / total_amount * 100, 2), 0), 100) as rate
          from public.purchase_orders
         where tax_amount is not null and total_amount > 0) r
 where l.purchase_order_id = r.id
   and l.tax_rate is null;

alter table public.purchase_order_lines drop constraint if exists purchase_order_lines_tax_rate_check;
alter table public.purchase_order_lines add constraint purchase_order_lines_tax_rate_check
    check (tax_rate is null or (tax_rate >= 0 and tax_rate <= 100));

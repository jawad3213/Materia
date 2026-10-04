-- Return-to-vendor columns linking a return to its goods receipt, purchase-order line and price
-- (mirrors the main migration V20261004_03__create_return_to_vendor_tables.sql).
alter table public.return_to_vendor add column if not exists currency_code varchar(3);
alter table public.return_to_vendor_lines add column if not exists purchase_order_line_id varchar(100);
alter table public.return_to_vendor_lines add column if not exists material_id varchar(100);
alter table public.return_to_vendor_lines add column if not exists unit_price numeric(19,4);

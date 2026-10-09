-- Goods receipts stored the supplier id as free text while every other module stores a uuid. Make it a uuid, so a
-- receipt can only name a well-formed supplier id and joins with orders, invoices and payments compare like types.
-- A blank value becomes null; a malformed one makes this migration fail rather than silently lose the link.
alter table public.goods_receipts
    alter column supplier_id type uuid using nullif(trim(supplier_id), '')::uuid;

alter table public.goods_receipt_lines
    alter column supplier_id type uuid using nullif(trim(supplier_id), '')::uuid;

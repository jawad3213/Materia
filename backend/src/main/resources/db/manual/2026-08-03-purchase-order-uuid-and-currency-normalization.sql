-- Manual migration for purchase order reference IDs and currency normalization.
-- Assumes existing reference values are valid UUID strings where present.

begin;

update purchase_orders
set currency_code = upper(currency_code)
where currency_code is not null;

update purchase_order_lines pol
set currency_code = upper(coalesce(pol.currency_code, po.currency_code))
from purchase_orders po
where pol.purchase_order_id = po.id;

alter table purchase_orders
    alter column requisition_id type uuid using nullif(trim(requisition_id), '')::uuid,
    alter column supplier_id type uuid using nullif(trim(supplier_id), '')::uuid;

alter table purchase_order_lines
    alter column requisition_line_id type uuid using nullif(trim(requisition_line_id), '')::uuid,
    alter column supplier_id type uuid using nullif(trim(supplier_id), '')::uuid;

commit;

-- Documents reference each other across modules (a receipt its order, an invoice its receipt, a payment its
-- invoices...). Those references were text columns without constraints, so nothing stopped a document from pointing
-- to a row that does not exist, and the same identifier was uuid in one table and varchar in another.
--
-- For every reference this migration:
--   1. converts a varchar column to uuid;
--   2. clears an optional reference that is not a UUID or points to a missing row (it could never be followed);
--      a required reference in that state stops the migration with the table and the number of rows to fix;
--   3. adds a foreign key to the referenced table.
--
-- The keys are DEFERRABLE INITIALLY IMMEDIATE: checked at each statement as usual, but a transaction that inserts
-- documents referencing each other in one go (the demo data seeder) may defer them to its commit.
do $$
declare
    ref record;
    uuid_pattern constant text := '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$';
    column_type text;
    broken bigint;
    constraint_name text;
begin
    for ref in
        select * from (values
            -- (table, column, referenced table, required)
            ('goods_receipts',             'purchase_order_id',      'purchase_orders',            true),
            ('goods_receipts',             'supplier_id',            'suppliers',                  false),
            ('goods_receipt_lines',        'purchase_order_line_id', 'purchase_order_lines',       false),
            ('goods_receipt_lines',        'supplier_id',            'suppliers',                  false),
            ('goods_receipt_lines',        'material_id',            'materials',                  false),
            ('invoices',                   'purchase_order_id',      'purchase_orders',            false),
            ('invoices',                   'goods_receipt_id',       'goods_receipts',             false),
            ('invoices',                   'supplier_id',            'suppliers',                  true),
            ('invoice_lines',              'purchase_order_line_id', 'purchase_order_lines',       false),
            ('invoice_lines',              'goods_receipt_line_id',  'goods_receipt_lines',        false),
            ('payments',                   'supplier_id',            'suppliers',                  true),
            ('payment_lines',              'invoice_id',             'invoices',                   true),
            ('payment_lines',              'supplier_id',            'suppliers',                  true),
            ('return_to_vendor',           'goods_receipt_id',       'goods_receipts',             false),
            ('return_to_vendor',           'purchase_order_id',      'purchase_orders',            false),
            ('return_to_vendor',           'supplier_id',            'suppliers',                  false),
            ('return_to_vendor_lines',     'goods_receipt_line_id',  'goods_receipt_lines',        false),
            ('return_to_vendor_lines',     'purchase_order_line_id', 'purchase_order_lines',       false),
            ('return_to_vendor_lines',     'material_id',            'materials',                  false),
            ('purchase_orders',            'supplier_id',            'suppliers',                  true),
            ('purchase_orders',            'requisition_id',         'purchase_requisitions',      false),
            ('purchase_order_lines',       'requisition_line_id',    'purchase_requisition_lines', false),
            ('purchase_order_lines',       'material_id',            'materials',                  false),
            ('purchase_order_lines',       'supplier_id',            'suppliers',                  false),
            ('purchase_requisitions',      'purchase_order_id',      'purchase_orders',            false),
            ('purchase_requisition_lines', 'material_id',            'materials',                  false),
            ('purchase_requisition_lines', 'supplier_id',            'suppliers',                  false),
            ('materials',                  'category_id',            'categories',                 false),
            ('materials',                  'supplier_id',            'suppliers',                  false),
            ('categories',                 'parent_id',              'categories',                 false),
            ('employees',                  'user_id',                'users',                      false)
        ) as refs(table_name, column_name, parent_table, required)
    loop
        select data_type into column_type
          from information_schema.columns
         where table_schema = 'public' and table_name = ref.table_name and column_name = ref.column_name;
        if column_type is null then
            raise exception 'Column public.%.% does not exist', ref.table_name, ref.column_name;
        end if;

        -- References that cannot be followed: not a UUID, or no such row.
        execute format(
            'select count(*) from public.%I t where t.%I is not null and (t.%I::text !~ %L '
                || 'or not exists (select 1 from public.%I p where p.id::text = lower(t.%I::text)))',
            ref.table_name, ref.column_name, ref.column_name, uuid_pattern, ref.parent_table, ref.column_name)
            into broken;
        if broken > 0 then
            if ref.required then
                raise exception '% row(s) of public.% have a % that is not an existing %; fix them before migrating',
                    broken, ref.table_name, ref.column_name, ref.parent_table;
            end if;
            execute format(
                'update public.%I t set %I = null where t.%I is not null and (t.%I::text !~ %L '
                    || 'or not exists (select 1 from public.%I p where p.id::text = lower(t.%I::text)))',
                ref.table_name, ref.column_name, ref.column_name, ref.column_name, uuid_pattern,
                ref.parent_table, ref.column_name);
            raise notice 'Cleared % unresolvable reference(s) in public.%.%', broken, ref.table_name, ref.column_name;
        end if;

        if column_type <> 'uuid' then
            execute format('alter table public.%I alter column %I type uuid using %I::uuid',
                ref.table_name, ref.column_name, ref.column_name);
        end if;

        constraint_name := left('fk_' || ref.table_name || '_' || ref.column_name, 63);
        if not exists (select 1 from pg_constraint where conname = constraint_name
                         and conrelid = format('public.%I', ref.table_name)::regclass) then
            execute format(
                'alter table public.%I add constraint %I foreign key (%I) references public.%I (id) '
                    || 'deferrable initially immediate',
                ref.table_name, constraint_name, ref.column_name, ref.parent_table);
        end if;
    end loop;
end $$;

-- Columns used to follow the new keys that had no index yet.
create index if not exists idx_rtv_goods_receipt_id on public.return_to_vendor (goods_receipt_id);
create index if not exists idx_rtv_purchase_order_id on public.return_to_vendor (purchase_order_id);
create index if not exists idx_rtv_supplier_id on public.return_to_vendor (supplier_id);
create index if not exists idx_invoice_goods_receipt_id on public.invoices (goods_receipt_id);
create index if not exists idx_payment_line_invoice_id on public.payment_lines (invoice_id);
create index if not exists idx_invoice_line_po_line_id on public.invoice_lines (purchase_order_line_id);
create index if not exists idx_pr_purchase_order_id on public.purchase_requisitions (purchase_order_id);

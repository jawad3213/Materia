-- Convert legacy text-based purchase order reference columns to UUID before Hibernate updates the schema.
-- Safe to run on fresh databases because each block checks that the target table and columns exist first.

do
$$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'purchase_orders'
    ) then
        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_orders'
              and column_name = 'currency_code'
        ) then
            update public.purchase_orders
            set currency_code = upper(currency_code)
            where currency_code is not null;
        end if;

        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_orders'
              and column_name = 'requisition_id'
              and data_type <> 'uuid'
        ) then
            execute 'alter table public.purchase_orders alter column requisition_id type uuid using nullif(trim(requisition_id), '''')::uuid';
        end if;

        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_orders'
              and column_name = 'supplier_id'
              and data_type <> 'uuid'
        ) then
            execute 'alter table public.purchase_orders alter column supplier_id type uuid using nullif(trim(supplier_id), '''')::uuid';
        end if;
    end if;
end
$$;

do
$$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'purchase_order_lines'
    ) then
        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_order_lines'
              and column_name = 'currency_code'
        ) and exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_order_lines'
              and column_name = 'purchase_order_id'
        ) then
            update public.purchase_order_lines pol
            set currency_code = upper(coalesce(pol.currency_code, po.currency_code))
            from public.purchase_orders po
            where pol.purchase_order_id = po.id;
        end if;

        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_order_lines'
              and column_name = 'requisition_line_id'
              and data_type <> 'uuid'
        ) then
            execute 'alter table public.purchase_order_lines alter column requisition_line_id type uuid using nullif(trim(requisition_line_id), '''')::uuid';
        end if;

        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'purchase_order_lines'
              and column_name = 'supplier_id'
              and data_type <> 'uuid'
        ) then
            execute 'alter table public.purchase_order_lines alter column supplier_id type uuid using nullif(trim(supplier_id), '''')::uuid';
        end if;
    end if;
end
$$;

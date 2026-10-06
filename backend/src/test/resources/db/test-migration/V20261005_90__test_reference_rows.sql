-- Test-only rows that fixtures reference by default (support/fixtures/ReferenceRows). Since V20261005_04 the
-- database refuses references to rows that do not exist, so documents built by fixtures need a real category,
-- supplier and material to point at.
insert into public.categories (id, code, name, status, level, path, category_type, created_at, version)
values ('00000000-0000-4000-8000-00000000ca01', 'CAT-REF-0001', 'Reference category', 'ACTIVE', 0, '/CAT-REF-0001/',
        'RAW_MATERIAL_CAT', now(), 0);

insert into public.suppliers (id, code, name, status, currency_code, payment_delay, created_at, version)
values ('00000000-0000-4000-8000-00000000b001', 'SUP-REF-0001', 'Reference supplier', 'ACTIVE', 'MAD', 30, now(), 0);

insert into public.materials (id, code, name, material_type, status, unit_of_measure, category_id, category_name,
                              supplier_id, supplier_name, current_stock, available_stock, stock_on_order,
                              created_at, version)
values ('00000000-0000-4000-8000-00000000a001', 'RMT-REF-0001', 'Reference material', 'RAW_MATERIAL', 'ACTIVE', 'PCE',
        '00000000-0000-4000-8000-00000000ca01', 'Reference category',
        '00000000-0000-4000-8000-00000000b001', 'Reference supplier', 0, 0, 0, now(), 0);

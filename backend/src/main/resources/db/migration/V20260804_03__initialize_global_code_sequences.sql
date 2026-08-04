do $$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'code_sequences'
    ) and not exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'global_code_sequences'
    ) then
        alter table public.code_sequences rename to global_code_sequences;
    end if;
end $$;

create table if not exists public.global_code_sequences (
    prefix varchar(10) primary key,
    next_val integer not null
);

do $$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'purchase_requisition_code_sequences'
    ) then
        insert into public.global_code_sequences (prefix, next_val)
        select prefix, next_val
        from public.purchase_requisition_code_sequences
        on conflict (prefix) do update
        set next_val = greatest(public.global_code_sequences.next_val, excluded.next_val);

        drop table public.purchase_requisition_code_sequences;
    end if;
end $$;

insert into public.global_code_sequences (prefix, next_val)
select
    'GR',
    coalesce(max(cast(substring(receipt_code from 4) as integer)), 0) + 1
from public.goods_receipts
where receipt_code ~ '^GR-[0-9]{4}$'
on conflict (prefix) do update
set next_val = greatest(public.global_code_sequences.next_val, excluded.next_val);

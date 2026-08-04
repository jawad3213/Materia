-- Rename the shared sequence table and merge requisition counters into it.
-- Run this once in environments that already contain code generation data.

begin;

alter table if exists code_sequences rename to global_code_sequences;

create table if not exists global_code_sequences (
    prefix varchar(10) primary key,
    next_val integer not null
);

insert into global_code_sequences (prefix, next_val)
select prefix, next_val
from purchase_requisition_code_sequences
on conflict (prefix) do update
set next_val = greatest(global_code_sequences.next_val, excluded.next_val);

drop table if exists purchase_requisition_code_sequences;

commit;

alter table if exists public.purchase_orders
    add column if not exists assigned_to varchar(100),
    add column if not exists assigned_to_name varchar(255),
    add column if not exists assigned_at timestamp,
    add column if not exists assigned_by varchar(100),
    add column if not exists assigned_by_name varchar(255);

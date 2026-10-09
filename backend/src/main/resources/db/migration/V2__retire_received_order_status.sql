-- RECEIVED was a legacy order status: no transition produced it any more, a full receipt completes the order.
-- Orders still stored with it are closed as COMPLETED (fully delivered), and the status check no longer allows it.
update public.purchase_orders
   set status = 'COMPLETED',
       delivery_status = 'DELIVERED',
       received_date = coalesce(received_date, updated_at::date, created_at::date)
 where status = 'RECEIVED';

alter table public.purchase_orders drop constraint if exists purchase_orders_status_check;
alter table public.purchase_orders add constraint purchase_orders_status_check
    check (status in ('DRAFT','SUBMITTED','CONFIRMED','READY_FOR_RECEIPT',
                      'PARTIALLY_RECEIVED','COMPLETED','CANCELLED','REJECTED'));

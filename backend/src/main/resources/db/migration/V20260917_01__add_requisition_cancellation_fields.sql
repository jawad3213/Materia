-- Add cancellation fields to purchase requisitions
ALTER TABLE purchase_requisitions ADD COLUMN IF NOT EXISTS cancelled_date date;
ALTER TABLE purchase_requisitions ADD COLUMN IF NOT EXISTS cancellation_reason varchar(1000);

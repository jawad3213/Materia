-- Manual cleanup script for environments that previously relied on Hibernate auto-update.
-- Run this after confirming the application is on the hard-delete model.

DROP TABLE IF EXISTS category_children;

ALTER TABLE categories DROP COLUMN IF EXISTS material_count;
ALTER TABLE categories DROP COLUMN IF EXISTS sub_category_count;
ALTER TABLE categories DROP COLUMN IF EXISTS total_items;
ALTER TABLE categories DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE categories DROP COLUMN IF EXISTS deleted_by;

ALTER TABLE materials DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE materials DROP COLUMN IF EXISTS deleted_by;

ALTER TABLE suppliers DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE suppliers DROP COLUMN IF EXISTS deleted_by;

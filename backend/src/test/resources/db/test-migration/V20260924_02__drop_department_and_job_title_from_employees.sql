-- Migration: Drop department and job_title columns from employees table
drop index if exists public.idx_employees_department;

alter table public.employees
    drop column if exists department,
    drop column if exists job_title;

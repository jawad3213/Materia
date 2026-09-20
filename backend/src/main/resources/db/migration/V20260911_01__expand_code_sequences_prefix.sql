-- Expand global_code_sequences.prefix column length to support yearly sequence prefixes (e.g., MATERIAL-2026, MAT-2026)
ALTER TABLE public.global_code_sequences ALTER COLUMN prefix TYPE VARCHAR(30);

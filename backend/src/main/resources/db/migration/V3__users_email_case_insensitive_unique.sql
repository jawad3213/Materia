-- E-mail addresses are compared case-insensitively everywhere (sign-in lower-cases them), so the database must
-- refuse two accounts that differ only by case (FINDING-018).
-- Stored addresses are first normalised, where that does not collide with another account.
update public.users u
   set email = lower(trim(u.email))
 where u.email <> lower(trim(u.email))
   and not exists (select 1 from public.users o
                    where o.id <> u.id and lower(trim(o.email)) = lower(trim(u.email)));

create unique index uk_users_email_lower on public.users (lower(email));

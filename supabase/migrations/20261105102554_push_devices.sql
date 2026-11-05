create table public.device_tokens (
 user_id uuid not null references auth.users(id) on delete cascade,
 token text not null check(length(token) between 20 and 4096),
 primary key(user_id,token)
);
alter table public.device_tokens enable row level security;
create policy own_device on public.device_tokens for all to authenticated
 using(user_id=auth.uid()) with check(user_id=auth.uid());
grant select,insert,update,delete on public.device_tokens to authenticated;
create index reports_owner on public.reports(user_id,created_at desc);
-- Legacy password hashes are retained for controlled migration, never exposed to clients.
revoke all on public.app_users, public.tickets, public.incidents from anon,authenticated;

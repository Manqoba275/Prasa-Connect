drop policy if exists "Service role manages app users" on public.app_users;
create policy "Service role manages app users"
on public.app_users
for all
to service_role
using (true)
with check (true);

drop policy if exists "Service role manages tickets" on public.tickets;
create policy "Service role manages tickets"
on public.tickets
for all
to service_role
using (true)
with check (true);

drop policy if exists "Service role manages incidents" on public.incidents;
create policy "Service role manages incidents"
on public.incidents
for all
to service_role
using (true)
with check (true);

revoke execute on function public.rls_auto_enable() from anon, authenticated;

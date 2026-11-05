-- Legacy records remain intact. New records belong to Supabase Auth users.
create table public.profiles (
 id uuid primary key references auth.users(id) on delete cascade,
 full_name text not null default '', mobile text not null default '',
 language text not null default 'en' check(language in ('en','zu','af')),
 notifications_enabled boolean not null default true, offline_sync_enabled boolean not null default true
);
alter table public.profiles enable row level security;
create policy own_profile on public.profiles for all to authenticated using(id=auth.uid()) with check(id=auth.uid());
grant select,insert,update on public.profiles to authenticated;
create table public.bookings (
 id uuid primary key default gen_random_uuid(), user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
 schedule_id uuid not null references public.schedules(id), request_id uuid not null,
 route text not null, train text not null, fare numeric(10,2) not null,
 status text not null default 'Active', created_at timestamptz not null default now(), unique(user_id,request_id)
);
alter table public.bookings enable row level security;
create policy own_bookings on public.bookings for select to authenticated using(user_id=auth.uid());
grant select on public.bookings to authenticated;
create table public.reports (
 id uuid primary key, user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
 type text not null check(length(trim(type)) between 1 and 100),
 location text not null check(length(trim(location)) between 1 and 300),
 description text not null check(length(trim(description)) between 10 and 3000),
 status text not null default 'Submitted', created_at timestamptz not null default now()
);
alter table public.reports enable row level security;
create policy own_reports_read on public.reports for select to authenticated using(user_id=auth.uid());
create policy own_reports_insert on public.reports for insert to authenticated with check(user_id=auth.uid());
grant select,insert on public.reports to authenticated;
create table public.service_alerts (id uuid primary key default gen_random_uuid(),title text not null,message text not null,created_at timestamptz not null default now());
alter table public.service_alerts enable row level security;
create policy read_alerts on public.service_alerts for select to authenticated using(true);
grant select on public.service_alerts to authenticated;
-- Caller and fare are server-owned. Retries cannot duplicate a booking.
create function public.book_journey(p_schedule uuid,p_request uuid) returns public.bookings
language plpgsql security definer set search_path='' as $$
declare journey public.schedules; ticket public.bookings;
begin
 if auth.uid() is null then raise exception 'Authentication required'; end if;
 select * into journey from public.schedules where id=p_schedule;
 if not found then raise exception 'Schedule not found'; end if;
 insert into public.bookings(user_id,schedule_id,request_id,route,train,fare)
 values(auth.uid(),journey.id,p_request,journey.origin || ' -> ' || journey.destination,journey.train,28.00)
 on conflict(user_id,request_id) do nothing;
 select * into ticket from public.bookings where user_id=auth.uid() and request_id=p_request;
 return ticket;
end $$;
revoke all on function public.book_journey(uuid,uuid) from public,anon;
grant execute on function public.book_journey(uuid,uuid) to authenticated;

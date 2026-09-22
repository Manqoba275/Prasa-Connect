create extension if not exists pgcrypto;

create table if not exists public.app_users (
  id uuid primary key default gen_random_uuid(),
  full_name text not null,
  email text unique not null,
  mobile text not null,
  password_hash text not null,
  language text not null default 'English',
  notifications_enabled boolean not null default true,
  offline_sync_enabled boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.schedules (
  id uuid primary key default gen_random_uuid(),
  train text not null,
  origin text not null,
  destination text not null,
  depart_time text not null,
  arrive_time text not null,
  platform text not null,
  status text not null default 'On time',
  created_at timestamptz not null default now()
);

create table if not exists public.tickets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.app_users(id) on delete cascade,
  schedule_id uuid references public.schedules(id) on delete set null,
  route text not null,
  train text not null,
  fare numeric(10,2) not null default 28.00,
  qr_code text not null,
  status text not null default 'Active',
  created_at timestamptz not null default now()
);

create table if not exists public.incidents (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.app_users(id) on delete cascade,
  type text not null,
  location text not null,
  description text not null,
  status text not null default 'Submitted',
  created_at timestamptz not null default now()
);

create index if not exists idx_app_users_email on public.app_users (lower(email));
create index if not exists idx_schedules_route on public.schedules (lower(origin), lower(destination));
create index if not exists idx_tickets_user on public.tickets (user_id, created_at desc);
create index if not exists idx_incidents_user on public.incidents (user_id, created_at desc);

alter table public.app_users enable row level security;
alter table public.schedules enable row level security;
alter table public.tickets enable row level security;
alter table public.incidents enable row level security;

drop policy if exists "Public can read schedules" on public.schedules;
create policy "Public can read schedules"
on public.schedules for select
to anon, authenticated
using (true);

insert into public.schedules (train, origin, destination, depart_time, arrive_time, platform, status)
select * from (values
  ('T0507', 'Cape Town', 'Bellville', '08:45', '09:15', '2', 'On time'),
  ('T0509', 'Cape Town', 'Bellville', '09:15', '09:45', '1', 'On time'),
  ('T0612', 'Johannesburg', 'Pretoria', '10:00', '10:58', '4', 'Delayed 5 min'),
  ('T0718', 'Pretoria', 'Ekurhuleni', '11:30', '12:15', '3', 'On time'),
  ('T0821', 'Pretoria', 'Johannesburg', '13:05', '14:02', '5', 'On time'),
  ('T0904', 'Germiston', 'Johannesburg', '15:20', '15:55', '1', 'On time')
) as seed(train, origin, destination, depart_time, arrive_time, platform, status)
where not exists (
  select 1 from public.schedules s where s.train = seed.train and s.depart_time = seed.depart_time
);

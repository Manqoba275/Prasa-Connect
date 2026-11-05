alter table public.reports add column if not exists photo_path text;
alter table public.reports add constraint reports_photo_path_owner
check (photo_path is null or photo_path like user_id::text || '/%');

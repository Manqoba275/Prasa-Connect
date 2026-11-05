insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('incident-photos', 'incident-photos', false, 5242880, array['image/jpeg','image/png','image/webp'])
on conflict (id) do update set public = false, file_size_limit = 5242880, allowed_mime_types = array['image/jpeg','image/png','image/webp'];

create policy "Users read own incident photos"
on storage.objects for select to authenticated
using (bucket_id = 'incident-photos' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "Users upload own incident photos"
on storage.objects for insert to authenticated
with check (bucket_id = 'incident-photos' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "Users delete own incident photos"
on storage.objects for delete to authenticated
using (bucket_id = 'incident-photos' and (storage.foldername(name))[1] = (select auth.uid())::text);

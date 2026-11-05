insert into public.app_users (full_name, email, mobile, password_hash, language)
values (
  'Nonhlanhla Chirwa',
  'nonhlanhla@prasa.demo',
  '0712345678',
  encode(digest('password123', 'sha256'), 'hex'),
  'Tshivenda'
)
on conflict (email) do update set
  full_name = excluded.full_name,
  mobile = excluded.mobile,
  password_hash = excluded.password_hash,
  language = excluded.language;

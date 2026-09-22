# PRASA Connect Supabase Backend

Project: `Prasa-Connect`

Hosted API:

```text
https://awbgflgcqjofsnagkzou.supabase.co/functions/v1/prasa-api
```

Created tables:

- `public.app_users`
- `public.schedules`
- `public.tickets`
- `public.incidents`

API routes:

- `GET /`
- `POST /auth/register`
- `POST /auth/login`
- `GET /schedules?from=Cape Town&to=Bellville`
- `POST /bookings`
- `GET /users/{userId}/tickets`
- `POST /incidents`
- `PATCH /users/{userId}/settings`

The Edge Function uses custom auth logic and stores passwords as SHA-256 hashes for the student prototype. For production, use Supabase Auth or a stronger password hashing scheme.

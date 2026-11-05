# Supabase backend

The current source replaces the legacy SHA-256/custom-user API with Supabase Auth and user-scoped RLS.

The migrations in this repository are aligned to the version IDs recorded by the connected `Prasa-Connect` project. The legacy demo-user seed is archived outside the migration directory and must not be deployed.

New tables: profiles, bookings, reports, service_alerts, device_tokens. Existing schedules remain. Legacy app_users/tickets/incidents are preserved and inaccessible to ordinary clients. Legacy logins must register through Supabase Auth.

Functions:
- prasa-api: public health; all data routes require a valid Auth user access token.
- send-alert: POST only, verified user plus ALERT_ADMIN_USER_IDS allowlist; FCM_SERVICE_ACCOUNT_JSON is stored only as an edge secret.

The Android publishable key is not a privileged credential. Never commit service-role keys, personal access tokens, DB passwords or Firebase service-account JSON.

Deployment was performed on 5 November 2026: `authenticated_app` and `push_devices` were applied; `prasa-api` version 3 and `send-alert` version 1 are active. The six existing sample schedules are retained. Email/Google authentication, FCM secrets and live two-user acceptance tests still need configuration and verification. See the audit and verification record for remaining evidence.

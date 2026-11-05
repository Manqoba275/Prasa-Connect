# Supabase backend

The current source replaces the legacy SHA-256/custom-user API with Supabase Auth and user-scoped RLS.

Read [deployment instructions](../docs/DEPLOYMENT.md) before applying migrations. Back up and compare remote migration history first; previously deployed timestamps may differ. Do not reset a populated database.

New tables: profiles, bookings, reports, service_alerts, device_tokens. Existing schedules remain. Legacy app_users/tickets/incidents are preserved and inaccessible to ordinary clients. Legacy logins must register through Supabase Auth.

Functions:
- prasa-api: public health; all data routes require a valid Auth user access token.
- send-alert: POST only, verified user plus ALERT_ADMIN_USER_IDS allowlist; FCM_SERVICE_ACCOUNT_JSON is stored only as an edge secret.

The Android publishable key is not a privileged credential. Never commit service-role keys, personal access tokens, DB passwords or Firebase service-account JSON.

Deployment has not been verified in this session. See the main README for API routes and the audit for remaining acceptance evidence.

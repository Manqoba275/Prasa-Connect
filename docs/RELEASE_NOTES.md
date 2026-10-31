# 2.0 - unreleased

- Replaced local demo authentication with Supabase Auth and encrypted persisted sessions.
- Added authenticated schedule, booking, profile and incident API calls and owner-scoped database policies.
- Added genuine QR encoding for academic booking records and retry-safe booking creation.
- Added durable offline incident queue, cached reads and background/manual sync.
- Added account-scoped favourite routes with offline persistence and timetable navigation.
- Added Google PKCE sign-in and Firebase notification integration requiring external configuration.
- Added English/Afrikaans labels, updated branding, validation tests and CI reports.

Breaking change: legacy custom app_users passwords are not Supabase Auth credentials. Existing users must register/confirm a new Auth account. Legacy records are preserved but are not automatically linked or exposed. A verified ownership migration is required to recover them.

Known limitations and release gates: see PART2_PART3_AUDIT.md. This is an unverified source update, not a published release.

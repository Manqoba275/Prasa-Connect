# 2.0 - unreleased

- Replaced local demo authentication with Supabase Auth and encrypted persisted sessions.
- Added authenticated schedule, booking, profile and incident API calls and owner-scoped database policies.
- Added genuine QR encoding for academic booking records and retry-safe booking creation.
- Added durable offline incident queue, cached reads and background/manual sync.
- Added account-scoped favourite routes with offline persistence and timetable navigation.
- Added Google PKCE sign-in and Firebase notification integration requiring external configuration.
- Added English/Afrikaans labels, updated branding, validation tests and CI reports.
- Added API 24/35 CI coverage and explicit SQLite Closeable compatibility after minimum-version testing.
- Reconciled newer main-branch validation and deployment checks with the authenticated implementation.
- Added persistent 15-minute network-constrained background recovery for queued incident reports, alongside immediate reconnect work.
- Kept a consistent PRASA light theme on devices using system dark mode, with softer blue, gold and cool-neutral surfaces.
- Added show/hide password control and clearer Supabase Google provider/callback errors.
- Added the official Gauteng rail map to the dashboard and optional private incident-photo upload to Supabase Storage.
- Linked incident photo paths to authenticated report records; upload is limited to JPEG/PNG/WebP files up to 5 MB.
- Added an authenticated Supabase-to-Firebase custom-token bridge and per-account Firestore saved-route sync with deny-by-default rules. Firestore remains unavailable until its Google Cloud API is enabled, a database is verified/provisioned, and rules are deployed.

Breaking change: legacy custom app_users passwords are not Supabase Auth credentials. Existing users must register/confirm a new Auth account. Legacy records are preserved but are not automatically linked or exposed. A verified ownership migration is required to recover them.

Known limitations and release gates: see PART2_PART3_AUDIT.md and VERIFICATION.md. This is not yet a fresh, locally compiled APK or a published release; the latest build attempt ran out of workstation memory. Firestore provisioning, Google OAuth client correction, real-device acceptance tests, signed release packaging and submission evidence remain open.

# 2.0 - unreleased

- Replaced local demo authentication with Supabase Auth and encrypted persisted sessions.
- Added authenticated schedule, booking, profile and incident API calls and owner-scoped database policies.
- Added genuine QR encoding for academic booking records and retry-safe booking creation.
- Added durable offline incident queue, cached reads and background/manual sync.
- Added account-scoped favourite routes with offline persistence and timetable navigation.
- Added Google PKCE sign-in and Firebase notification integration requiring external configuration.
- Added English, Afrikaans and isiZulu labels, updated branding, validation tests and CI reports.
- Added API 24/35 CI coverage and explicit SQLite Closeable compatibility after minimum-version testing.
- Reconciled newer main-branch validation and deployment checks with the authenticated implementation.
- Added persistent 15-minute network-constrained background recovery for queued incident reports, alongside immediate reconnect work.
- Kept a consistent PRASA light theme on devices using system dark mode, with softer blue, gold and cool-neutral surfaces.
- Added show/hide password control and clearer Supabase Google provider/callback errors.
- Added the official Gauteng rail map to the dashboard and optional private incident-photo upload to Supabase Storage.
- Linked incident photo paths to authenticated report records; upload is limited to JPEG/PNG/WebP files up to 5 MB.
- Added an authenticated Supabase-to-Firebase custom-token bridge and per-account Firestore saved-route sync with deny-by-default rules. Firestore `prasa-app` is provisioned in `africa-south1`, and the user confirmed the Firestore rules deployment succeeded. End-to-end on-device/cross-account sync still needs evidence.
- Added automatic background route sync, including queued removals, while keeping the route list immediately available offline and suppressing Firebase sync status from the passenger UI.
- Added isiZulu translations and a settings preview when changing the language, plus a show/hide password control.

Breaking change: legacy custom app_users passwords are not Supabase Auth credentials. Existing users must register/confirm a new Auth account. Legacy records are preserved but are not automatically linked or exposed. A verified ownership migration is required to recover them.

Known limitations and release gates: see PART2_PART3_AUDIT.md and VERIFICATION.md. A current debug APK and unit tests build successfully; local lint analysis stalled, while GitHub Actions lint passed for revision 218ab37. Google sign-in is reported working; final phone acceptance tests, push-delivery evidence, signed release packaging, Play Console signup evidence and verification of the supplied video remain outstanding. Sample timetables are not an official PRASA live feed.

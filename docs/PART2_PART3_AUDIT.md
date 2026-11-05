# Part 2 and Part 3 audit

Audit updated: 5 November 2026. Source: OPSC POE.pdf, Part 2 requirements/rubric pp. 8-9 and 19-21; Part 3 pp. 10-12 and 22-25. See SUBMISSION_HANDOVER.md for the final evidence checklist.

**Source implemented does not mean deployed, compiled, tested or awarded marks.** The starting worktree contained demo authentication and simulated requests. Later commits on main added hosted requests with local fallback; these were reconciled and superseded by verified-session authentication and explicit failure handling. Older reports and PDFs are historical, not current verification evidence.

## Part 2

| Requirement | Source work | Remaining evidence |
| --- | --- | --- |
| Registration, login, logout; secure passwords | Supabase Auth REST, email confirmation handling, refresh tokens, encrypted Keystore-backed session | Test new account, duplicate email, incorrect password, confirmation and logout on a phone |
| Meaningful settings | Profile API, language, notifications and offline-sync preferences | Verify persistence after restart and across accounts |
| Hosted REST API and database integration | Android OkHttp repository, authenticated edge API, PostgreSQL migrations, user-owned RLS | Migrations and API deployed 5 Nov 2026; demonstrate live two-user isolation and persisted app records |
| Feature 1: timetable search | API query with account-scoped offline cache | Seeded timetables are examples, not a live PRASA feed; verify route queries |
| Feature 2: bookings and QR tickets | Server-derived fare, idempotent insert, genuine QR encoding of stored booking ID | Verify booking and QR decoding; academic tickets are not valid travel tickets |
| Feature 3: incident reporting | Validated API submission and own-report history; optional private Supabase Storage photo upload with per-user object paths and 5 MB MIME-limited files | Test photo upload and report persistence on a phone; verify another account cannot read the photo |
| Input validation and error handling | Client and SQL constraints, HTTP errors, network-only cache fallback | Invalid input, server outage, expired session and accessibility device tests |
| Tests and GitHub Actions | Validation unit tests; durable SQLite queue and intercepted HTTP authentication/session tests; build/lint/emulator workflows | See VERIFICATION.md for actual runs; live two-user API/provider end-to-end coverage remains |
| Demonstration video | Group supplied a YouTube URL, linked in README | Verify signed-out playback, clear voice-over, on-phone demonstration, required features and hosted auth/API/database evidence |
| README, comments, references, AI account | Updated README and AI_USE.md | Student review, actual commit history and submission URLs |

## Part 3

| Requirement | Source work | Remaining evidence |
| --- | --- | --- |
| SSO | Google OAuth browser flow with PKCE and deep-link callback | User reports successful sign-in after OAuth configuration; capture current phone evidence and verify logout/account switch |
| Offline operation and sync | SQLite report outbox and account-scoped favourite routes; WorkManager connected-network retry, immediate work and 15-minute recovery; route additions/removals sync silently to Firestore | Airplane mode, process restart, reconnect, cross-device update and no-duplicate demonstration |
| Real push notifications | FCM service, runtime permission, device API, admin-only FCM HTTP v1 sender; protected `send-alert` function deployed 5 Nov 2026 | Firebase app values and server secrets are configured; record foreground/background delivery and admin denial on a device |
| South African languages | English, Afrikaans and isiZulu interface labels | Fluent-speaker review; some backend errors and server content remain English |
| Feature 4 | Official Gauteng rail-map screen; map imagery added to dashboard | Device legibility/navigation evidence; not live GPS tracking |
| Feature 5 | Saved favourite routes with offline account-scoped SQLite persistence, queued route deletions, background Firestore sync, duplicate prevention and route search | Firestore database and rules deployed; verify cross-device sync and account isolation on devices |
| Final branding | Supplied PRASA logo in app and adaptive launcher icon; app label | Verify icon on device; obtain permission before any public PRASA-branded release |
| Publication preparation | Version 2.0 and environment-based signing configuration; publication checklist | Private keystore, signed APK/AAB, actual screenshots, privacy-policy hosting, any required store-upload evidence |
| README, release notes, AI account, final tag | Documentation prepared | Build/test success, commits and final tag after acceptance, not before |

## Not completed or not claimed

- Database migrations and both Edge Functions were deployed to Supabase on 5 November 2026. The private `incident-photos` Storage bucket, per-user policies and `reports.photo_path` column were added; the authenticated `prasa-api` was redeployed as version 7. Firebase app configuration and `FCM_SERVICE_ACCOUNT_JSON`/`ALERT_ADMIN_USER_IDS` secrets were subsequently configured. This is not yet proof of end-to-end authentication, user isolation, photo upload or phone notification delivery.
- Google OAuth was enabled in Supabase; after OAuth configuration, the user reports Google sign-in now works. Include a fresh device recording as evidence.
- Initial sandboxed Gradle execution was blocked; after access changed, Android compilation, unit tests, debug APK packaging and lint ran successfully. See VERIFICATION.md for final artifact/test status. These checks do not prove hosted integrations work.
- Real PRASA operational data, payments, live train tracking and official ticket validation are not provided. API records persist real user actions, but schedules/fares are labelled academic samples.
- Incident photos upload to private Supabase Storage and are linked to report records. Firestore `prasa-app` is provisioned in `africa-south1`, and owner-only rules were deployed. The app now queues route changes for connected background sync and suppresses Firebase configuration warnings in the passenger UI. Device end-to-end read/write and cross-device ownership checks remain.
- Part 1 feature parity gaps remain: no wallet/payment flow, official live train feed, or full searchable station catalogue. The current station list is derived from schedule origins and destinations. Review the Part 1 research/design document and complete remaining submission evidence against the roles and rubric.
- The demo URL was provided by the group and added to README; its playback, narration and coverage are not independently verified. Physical-device acceptance evidence, Play Console signup screenshots, signed release artifact and final tag remain outstanding.

Verified progress: the final merged source passed GitHub build/lint checks, 11 unit tests, and eight instrumentation tests on each of Android 7 and Android 15. See VERIFICATION.md for exact revisions and run links; provider and database deployment checks remain separate.

## Acceptance sequence (two separate test accounts)

1. Register A, confirm email, sign in; reject wrong password. Restart and confirm session recovery.
2. Search schedules, book once, retry the same request ID: one booking only. Decode ticket QR.
3. Sign in as B: A's tickets and reports must be absent. Try requesting/inserting A's ownership through REST: deny or return no rows.
4. Submit a report online and inspect its UUID in Supabase. Disable connectivity, submit another, kill/reopen app, reconnect: one stored report, no remaining queue item.
5. Disable auto-sync: queue remains until manual sync or re-enabling. Network/server/auth failures must not masquerade as successful submission.
6. Save language/profile settings; inspect database and restart. Check small screens, large fonts, navigation and empty/error states.
7. Complete Google SSO with a real account; reject unsolicited or expired callback.
8. Register FCM device, send an authorised test alert; verify background delivery. An ordinary user must receive 403 from send-alert. Test permission denial and notification opt-out.
9. Run CI, inspect reports, install signed release on a physical phone, capture actual screenshots, then create final release/tag.

# Part 2 and Part 3 audit

Audit date: 31 October 2026. Source: OPSC POE.pdf, Part 2 requirements/rubric pp. 8-9 and 19-21; Part 3 pp. 10-12 and 22-25. Video is excluded at the student's request.

**Source implemented does not mean deployed, compiled, tested or awarded marks.** The starting worktree contained demo authentication and simulated requests. Later commits on main added hosted requests with local fallback; these were reconciled and superseded by verified-session authentication and explicit failure handling. Older reports and PDFs are historical, not current verification evidence.

## Part 2

| Requirement | Source work | Remaining evidence |
| --- | --- | --- |
| Registration, login, logout; secure passwords | Supabase Auth REST, email confirmation handling, refresh tokens, encrypted Keystore-backed session | Deploy configuration; test new account, duplicate email, incorrect password, confirmation and logout on a phone |
| Meaningful settings | Profile API, language, notifications and offline-sync preferences | Verify persistence after restart and across accounts |
| Hosted REST API and database integration | Android OkHttp repository, authenticated edge API, PostgreSQL migrations, user-owned RLS | Apply reviewed migrations; deploy function; demonstrate two-user isolation and persisted records |
| Feature 1: timetable search | API query with account-scoped offline cache | Seeded timetables are examples, not a live PRASA feed; verify route queries |
| Feature 2: bookings and QR tickets | Server-derived fare, idempotent insert, genuine QR encoding of stored booking ID | Verify booking and QR decoding; academic tickets are not valid travel tickets |
| Feature 3: incident reporting | Validated API submission and own-report history | Submit and inspect persisted record; test denial of another user's records |
| Input validation and error handling | Client and SQL constraints, HTTP errors, network-only cache fallback | Invalid input, server outage, expired session and accessibility device tests |
| Tests and GitHub Actions | Validation unit tests; durable SQLite queue and intercepted HTTP authentication/session tests; build/lint/emulator workflows | See VERIFICATION.md for actual runs; live two-user API/provider end-to-end coverage remains |
| README, comments, references, AI account | Updated README and AI_USE.md | Student review, actual commit history and submission URLs |

## Part 3

| Requirement | Source work | Remaining evidence |
| --- | --- | --- |
| SSO | Google OAuth browser flow with PKCE and deep-link callback | Google provider/client configuration and real Google sign-in test |
| Offline operation and sync | SQLite report outbox; stable IDs; WorkManager network constraint/retry; manual sync | Airplane mode, process restart, reconnect and no-duplicate database demonstration |
| Real push notifications | FCM service, runtime permission, device API, admin-only FCM HTTP v1 sender | Firebase public app values, server service-account secret, administrator allowlist, foreground/background delivery test |
| Two South African languages | English and Afrikaans navigation/forms | Fluent-speaker review; some backend errors and server content remain English |
| Feature 4 | Official Gauteng rail-map screen | Device legibility/navigation evidence; not live GPS tracking |
| Feature 5 | Saved favourite routes with offline account-scoped SQLite persistence, duplicate prevention, remove and reopen-search actions | Verify navigation and account isolation; confirm chosen features match the accepted Part 1 proposal |
| Final branding | Supplied PRASA logo in app and adaptive launcher icon; app label | Verify icon on device; obtain permission before any public PRASA-branded release |
| Publication preparation | Version 2.0 and environment-based signing configuration; publication checklist | Private keystore, signed APK/AAB, actual screenshots, privacy-policy hosting, any required store-upload evidence |
| README, release notes, AI account, final tag | Documentation prepared | Build/test success, commits and final tag after acceptance, not before |

## Not completed or not claimed

- No hosted database or edge deployment was performed in this session: the Supabase connector is not available here.
- Initial sandboxed Gradle execution was blocked; after access changed, Android compilation, unit tests, debug APK packaging and lint ran successfully. See VERIFICATION.md for final artifact/test status. These checks do not prove hosted integrations work.
- Real PRASA operational data, payments, live train tracking and official ticket validation are not provided. API records persist real user actions, but schedules/fares are labelled academic samples.
- Blob upload and NoSQL integration appear among the Part 3 learning outcomes. This implementation uses PostgreSQL and SQLite, not NoSQL, and does not upload incident photos. Clarify whether these outcomes require additional assessed evidence; do not describe this stack as satisfying them.
- No physical-device verification, published store listing, screenshots, release tag or video was fabricated.

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

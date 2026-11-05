# Verification record

Date: 5 November 2026. This records observed checks, not a claim of complete POE compliance.

## Passed

- `deno check` for prasa-api and send-alert: passed locally.
- GitHub edge-function type checks: passed for the pushed commits.
- Android debug compilation, unit tests, lint and APK packaging: passed locally. Lint reported zero errors, 42 warnings and one hint; warnings include dependency-version suggestions, API/style suggestions and launcher/bitmap guidance.
- Instrumentation APK compilation/packaging: passed locally.
- [GitHub Android verification at 0d68819](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41175798701): build and emulator-test jobs both passed. This covers the Auth/offline integration before the subsequent favourites/account-switch additions.
- Final unit suite: 11 tests (seven PrasaRules tests and four validation/language tests), zero failures. The obsolete arithmetic example was removed on main and that removal is preserved.

## Current submission worktree check

- On 5 November 2026, `:app:testDebugUnitTest :app:assembleDebug` completed successfully against the submission worktree. The current local unit reports contain 13 tests (seven PrasaRules, four validation and two language-label tests), all passing. The debug APK is `app/build/outputs/apk/debug/app-debug.apk` (25,271,832 bytes); it is for testing, not signed publication.
- A combined `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` run entered lint analysis but produced no completion after several minutes and was stopped. Do not claim a fresh lint pass for this submission snapshot. Earlier CI lint results apply only to the revisions and workflow runs listed above.
- Google OAuth sign-in was reported working by the group after configuration. Firestore rules deployment was confirmed successful in the user's Firebase CLI output. These are user-reported/deployment checks; route syncing and notification delivery still need device evidence.

## Final revision

**Verified source revision: d79be22.** Its [pull-request verification run](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41197041394) passed all three jobs: build/lint/unit tests, eight instrumentation tests on API 24, and eight on API 35. Its [edge-function checks](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41197041415) also passed. Local unit reports independently record 11 tests with zero failures/errors. These results verify source/build behaviour, not live Google/Firebase setup or real-user acceptance flows.

Earlier verification history: revision 6d3f042 passed its [Android workflow](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41176187042), including eight Android 15 instrumentation tests. A local Android 7 run then exposed two SQLite test failures because the older SQLiteOpenHelper did not implement AutoCloseable. The explicit Closeable fix in b338e2d passed the [expanded CI matrix](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41196517589). The final merged revision is verified separately above.

HTTP tests intercept responses locally. They test client behaviour, not a deployed Supabase instance. SQLite tests verify queue/favourite persistence and account isolation. Local emulator runs were initially constrained by host memory; after running build and emulator sequentially, the login/registration screens were exercised and captured in evidence/. The failed pre-fix local storage tests and the successful post-fix CI tests are distinct evidence.

## Main-branch reconciliation

The worktree began at 3d895aa; main also contained e2e6a95, bbb117e and 840acf0. Those commits are merged, not discarded. SDK 36, wrapper executable mode, ignored screenshot files and missing-secret checks are retained. PrasaRules validation/tests are retained and tightened to the current policy. Obsolete client SHA-256 hashing and User-copy tests are replaced by validation tests; Supabase Auth now owns password storage and the profile API owns persisted settings. The legacy demo-user migration remains historical and does not create a Supabase Auth login.

## Live Supabase Deployment

- Database migrations `authenticated_app` and `push_devices` were applied to project `awbgflgcqjofsnagkzou`; the Supabase migration ledger records versions `20261105102543` and `20261105102554`. The repository migration filenames were aligned to those versions. Existing migration versions were aligned to the remote ledger; the unused demo-user seed is archived outside the deployment migration directory.
- `prasa-api` version 7, `send-alert` version 4 and `firebase-token` version 1 are deployed with function gateway JWT verification disabled because each function validates the bearer token itself. The `send-alert` allowlist and FCM service-account secrets are configured; notification delivery and custom-token exchange are not end-to-end verified.
- Private `incident-photos` Storage bucket verified present with 5 MB file limit and JPEG/PNG/WebP MIME restrictions. User-folder read/insert/delete policies and `reports.photo_path` are deployed; the authenticated API persists the object path. Phone upload and private photo viewing still require device verification.
- Historical note: an earlier Firebase CLI query returned HTTP 403 while Firestore API was disabled. The API was subsequently enabled, `prasa-app` was provisioned in `africa-south1`, and the user confirmed `firestore:rules` deployment completed. Android route read/write and cross-device sync remain to be tested.
- Android compilation could not start on this workstation: Windows reported only 128 MB free virtual memory and could not reserve the JVM's 96 MB G1 region. This is an environment limitation, not a compiler result.
- The database has six schedule rows. They are academic sample data, not an official live PRASA feed.

Smoke checks against the deployed endpoints returned API health version 2, `401` for unauthenticated schedule access, and `401` for unauthenticated alert sends. Authenticated app workflows have not yet been exercised against this project.

## Release Gates Still Open
- Test Supabase email confirmation and the now-working Google OAuth flow on the final phone build.
- Supply Firebase Android app configuration and server push credentials, then verify notification delivery on a real phone.
- Run the two-account live API/RLS acceptance checks in PART2_PART3_AUDIT.md.
- Confirm chosen extra features with the original proposal; verify Firestore live read/write and cross-device sync in `africa-south1`.
- Fluent-speaker translation review, physical-device screenshots, private release signing and any required Play Console evidence.
- Add the actual video and phone evidence before final hand-in.

Git commits use actual timestamps. No backdated history, fake users, fake live data or fabricated test results were added. The assessment changes are published on the submission branch; main is not changed by this work.

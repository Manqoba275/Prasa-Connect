# Verification record

Date: 5 November 2026. This records observed checks, not a claim of complete POE compliance.

## Passed

- `deno check` for prasa-api and send-alert: passed locally.
- GitHub edge-function type checks: passed for the pushed commits.
- Android debug compilation, unit tests, lint and APK packaging: passed locally. Lint reported zero errors, 42 warnings and one hint; warnings include dependency-version suggestions, API/style suggestions and launcher/bitmap guidance.
- Instrumentation APK compilation/packaging: passed locally.
- [GitHub Android verification at 0d68819](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41175798701): build and emulator-test jobs both passed. This covers the Auth/offline integration before the subsequent favourites/account-switch additions.
- Final unit suite: 11 tests (seven PrasaRules tests and four validation/language tests), zero failures. The obsolete arithmetic example was removed on main and that removal is preserved.

## Final revision

**Verified source revision: d79be22.** Its [pull-request verification run](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41197041394) passed all three jobs: build/lint/unit tests, eight instrumentation tests on API 24, and eight on API 35. Its [edge-function checks](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41197041415) also passed. Local unit reports independently record 11 tests with zero failures/errors. These results verify source/build behaviour, not live Google/Firebase setup or real-user acceptance flows.

Earlier verification history: revision 6d3f042 passed its [Android workflow](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41176187042), including eight Android 15 instrumentation tests. A local Android 7 run then exposed two SQLite test failures because the older SQLiteOpenHelper did not implement AutoCloseable. The explicit Closeable fix in b338e2d passed the [expanded CI matrix](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41196517589). The final merged revision is verified separately above.

HTTP tests intercept responses locally. They test client behaviour, not a deployed Supabase instance. SQLite tests verify queue/favourite persistence and account isolation. Local emulator runs were initially constrained by host memory; after running build and emulator sequentially, the login/registration screens were exercised and captured in evidence/. The failed pre-fix local storage tests and the successful post-fix CI tests are distinct evidence.

## Main-branch reconciliation

The worktree began at 3d895aa; main also contained e2e6a95, bbb117e and 840acf0. Those commits are merged, not discarded. SDK 36, wrapper executable mode, ignored screenshot files and missing-secret checks are retained. PrasaRules validation/tests are retained and tightened to the current policy. Obsolete client SHA-256 hashing and User-copy tests are replaced by validation tests; Supabase Auth now owns password storage and the profile API owns persisted settings. The legacy demo-user migration remains historical and does not create a Supabase Auth login.

## Live Supabase Deployment

- Database migrations `authenticated_app` and `push_devices` were applied to project `awbgflgcqjofsnagkzou`; the Supabase migration ledger records versions `20261105102543` and `20261105102554`. The repository migration filenames were aligned to those versions. Existing migration versions were aligned to the remote ledger; the unused demo-user seed is archived outside the deployment migration directory.
- `prasa-api` version 3 and `send-alert` version 1 are deployed with function gateway JWT verification disabled because each function validates the bearer token itself. The `send-alert` allowlist and FCM service-account secrets have not been configured, so notification delivery is not enabled or verified.
- The database has six schedule rows. They are academic sample data, not an official live PRASA feed.

Smoke checks against the deployed endpoints returned API health version 2, `401` for unauthenticated schedule access, and `401` for unauthenticated alert sends. Authenticated app workflows have not yet been exercised against this project.

## Release Gates Still Open
- Configure and test Supabase email confirmation and Google OAuth.
- Supply Firebase Android app configuration and server push credentials, then verify notification delivery on a real phone.
- Run the two-account live API/RLS acceptance checks in PART2_PART3_AUDIT.md.
- Confirm chosen extra features with the original proposal; address any assessed blob-storage/NoSQL learning outcomes.
- Fluent-speaker translation review, physical-device screenshots, private release signing and any required Play Console evidence.
- Video is excluded from this coding request, not marked completed.

Git commits use actual timestamps. No backdated history, fake users, fake live data or fabricated test results were added. The changes are on codex/part2-part3-integration, not automatically merged into main.

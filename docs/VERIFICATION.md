# Verification record

Date: 31 October 2026. This records observed checks, not a claim of complete POE compliance.

## Passed

- `deno check` for prasa-api and send-alert: passed locally.
- GitHub edge-function type checks: passed for the pushed commits.
- Android debug compilation, unit tests, lint and APK packaging: passed locally. Lint reported zero errors, 42 warnings and one hint; warnings include dependency-version suggestions, API/style suggestions and launcher/bitmap guidance.
- Instrumentation APK compilation/packaging: passed locally.
- [GitHub Android verification at 0d68819](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41175798701): build and emulator-test jobs both passed. This covers the Auth/offline integration before the subsequent favourites/account-switch additions.
- Unit suite: five tests (four validation/language tests plus the existing arithmetic test), zero failures.

## Final revision

The favourites/account-switch revision 6d3f042 passed its [Android workflow](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41176187042), including eight Android 15 instrumentation tests. A subsequent local Android 7 run exposed two SQLite test failures because the older SQLiteOpenHelper did not implement AutoCloseable. The explicit Closeable fix in b338e2d passed the [expanded CI matrix](https://github.com/Manqoba275/Prasa-Connect/actions/runs/41196517589): build plus API 24 and API 35 emulator jobs all succeeded. The later main-branch reconciliation should be checked against its own run, not attributed to this older run.

HTTP tests intercept responses locally. They test client behaviour, not a deployed Supabase instance. SQLite tests verify queue/favourite persistence and account isolation. Local emulator runs were initially constrained by host memory; after running build and emulator sequentially, the login/registration screens were exercised and captured in evidence/. The failed pre-fix local storage tests and the successful post-fix CI tests are distinct evidence.

## Main-branch reconciliation

The worktree began at 3d895aa; main also contained e2e6a95, bbb117e and 840acf0. Those commits are merged, not discarded. SDK 36, wrapper executable mode, ignored screenshot files and missing-secret checks are retained. PrasaRules validation/tests are retained and tightened to the current policy. Obsolete client SHA-256 hashing and User-copy tests are replaced by validation tests; Supabase Auth now owns password storage and the profile API owns persisted settings. The legacy demo-user migration remains historical and does not create a Supabase Auth login.

## Release gates still open

- Review remote migration history, apply database migrations and deploy edge functions.
- Configure and test Supabase email confirmation and Google OAuth.
- Supply Firebase app configuration and server push credentials, then verify notification delivery on a real phone.
- Run the two-account live API/RLS acceptance checks in PART2_PART3_AUDIT.md.
- Confirm chosen extra features with the original proposal; address any assessed blob-storage/NoSQL learning outcomes.
- Fluent-speaker translation review, physical-device screenshots, private release signing and any required Play Console evidence.
- Video is excluded from this coding request, not marked completed.

Git commits use actual timestamps. No backdated history, fake users, fake live data or fabricated test results were added. The changes are on codex/part2-part3-integration, not automatically merged into main.

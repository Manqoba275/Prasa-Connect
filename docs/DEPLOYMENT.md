# Deployment and verification

Do not paste tokens, passwords, keystores or service-account JSON into chat or commit them. Rotate the Supabase access token previously shown in a screenshot. A Supabase access token is not the database password: the database password was chosen or generated during project creation; reset it in database settings if unknown, then update dependent secrets.

## 1. Review the database before deployment

The source targets project `awbgflgcqjofsnagkzou`. Confirm it is the intended project and back up existing data before future schema changes. The local migration filenames have been aligned with the versions recorded by this connected project. The old demo-user seed is archived outside `supabase/migrations` and is intentionally excluded from deployment.

With Supabase CLI authenticated on your own machine, run:

```sh
supabase link --project-ref awbgflgcqjofsnagkzou
supabase migration list
supabase db push --dry-run
```

Confirm the migration list matches before pushing. Do not use `--include-all`, reset the database or blindly mark migrations applied. Supabase documents `migration repair`, but it edits history, not schema. Existing custom users are not migrated into Supabase Auth; create confirmed test accounts. Keep old rows private until a separately reviewed ownership migration is available.

For this project, the migrations creating profiles, bookings, reports, service_alerts and device_tokens have been applied, and both functions are deployed. These commands are for future reviewed deployments:

```sh
supabase db push
supabase functions deploy prasa-api --no-verify-jwt
supabase functions deploy send-alert --no-verify-jwt
```

`--no-verify-jwt` disables the gateway check only. Both functions verify bearer access tokens using Supabase Auth. The app API uses user-scoped clients/RLS; only the allowlisted alert sender uses the service role after authorization. Test missing/invalid credentials before release.

The old security migrations reference `public.rls_auto_enable()`, a project-specific function. They guard the revocation with an existence check for fresh installations. Already-applied migrations are not automatically re-run.

## 2. Email/password authentication

Enable email authentication in Supabase Auth. Keep confirmation enabled and configure reliable email delivery. Register with a real test email, follow its confirmation email and sign in. The app intentionally does not fake a successful session when confirmation is pending. Set a strong password policy in Auth configuration. Never store passwords in app_users or in the Android app.

The Android app contains only the project URL and publishable key. Never put a service-role key, personal access token or database password in an APK.

## 3. Google SSO

1. Create/select a Google Cloud project and configure the OAuth consent screen. Add test users while the application is in testing mode.
2. Create the web OAuth client required by Supabase's Google provider. Copy the exact provider callback URL shown by Supabase into Google's authorised redirect URIs.
3. Enable Google in Supabase Auth and enter the Google client ID and client secret there, not in Android source.
4. Add `prasaconnect://auth/callback` to the Supabase Auth redirect allowlist. The Android manifest already handles this URI.
5. Install the app, choose Continue with Google, approve in the browser and verify the callback returns to the app with an authenticated session. The implementation uses PKCE rather than trusting tokens from arbitrary callback URLs.

## 4. Firebase notifications

1. Create/select your Firebase project and register Android package `com.example.prasa`. If changing this package for publication, update Gradle, Firebase registration and tests together.
2. From the Firebase Android app configuration obtain its app ID, API key, project ID and sender/project number. These are public app configuration, not server credentials.
3. Set local Gradle properties (outside version control) or GitHub repository Actions **Variables** named `FIREBASE_APP_ID`, `FIREBASE_API_KEY`, `FIREBASE_PROJECT_ID`, `FIREBASE_SENDER_ID`. The build reads them; without them push is deliberately unconfigured. Rebuild/reinstall after changes.
4. Enable FCM HTTP v1 in Google Cloud. Create a restricted service account with permission to send FCM messages for that project. Store its JSON only as Supabase Edge Function secret `FCM_SERVICE_ACCOUNT_JSON`, through the dashboard secret editor or a protected local env file passed to `supabase secrets set --env-file ...`.
5. Add `ALERT_ADMIN_USER_IDS` as a Supabase secret containing comma-separated Supabase Auth user UUIDs permitted to send alerts. Ordinary app users must not be listed. Do not confuse UUIDs with email addresses or project IDs.
6. Sign into the installed app, allow notifications, and wait for WorkManager to register the FCM token in `device_tokens`.
7. Using an allowlisted administrator's **user access token**, POST JSON `{"title":"Test service notice","message":"Academic delivery test"}` to `/functions/v1/send-alert`. Send Authorization: Bearer USER_ACCESS_TOKEN and apikey: PUBLISHABLE_KEY. Use a local API client and do not record credentials in evidence screenshots.
8. Verify the response's sent/failed counts, service_alerts row and actual phone notification. A success count means FCM accepted the message, not proof the phone displayed it. Test with the app backgrounded and notification permission denied.

The sender sends generic service announcements only; it is suitable for a small academic test audience, not a large production broadcast queue. Notification opt-out and logout currently remove all registered device tokens for that account. A stale token from an offline logout must never be used to send personal information.

## 5. Firestore (Part 3 NoSQL read/write)

Firebase project: `prasa-4c7ba`. The app mirrors saved routes between SQLite and Firestore at `users/{supabaseUid}/saved_routes/{routeHash}`. The `firebase-token` Supabase Edge Function validates the Supabase session and issues a short-lived Firebase custom token with the same UID; `firestore.rules` denies all other data access. Do not deploy permissive test rules.

The Firestore API is enabled, a named Enterprise database `prasa-app` has been created in `africa-south1`, and its owner-only rules have been deployed. To verify the deployment later, run:

```powershell
npx -y firebase-tools@latest firestore:databases:list --project prasa-4c7ba
```

The Android client and Firebase CLI configuration target this named database. Confirm Firebase Authentication's custom-token provider is enabled and Android app values are configured before testing. To redeploy the locked-down rules after a reviewed change:

```powershell
npx -y firebase-tools@latest deploy --only firestore:rules --project prasa-4c7ba
```

Ensure Supabase Edge Function secret `FCM_SERVICE_ACCOUNT_JSON` is a valid service-account JSON for the same Firebase project and contains its signing key. `firebase-token` is deployed as version 1. The app queues favourite-route changes locally and WorkManager synchronizes them when connected, including deletions; errors retry silently. Verify a route on a second device/account and confirm one user cannot read another user's route path. Check API and function logs if token exchange returns 503. The build uses Firebase app configuration from local Gradle properties or GitHub Actions Variables; no service-account key belongs in Android or GitHub.

## 6. GitHub Actions

Repository Settings > Secrets and variables > Actions:

| Location | Name | Value |
| --- | --- | --- |
| Secret | SUPABASE_ACCESS_TOKEN | Newly generated, project-scoped deployment token |
| Secret | SUPABASE_DB_PASSWORD | Actual project database password, not the access token |
| Variable | FIREBASE_APP_ID | Firebase Android app ID |
| Variable | FIREBASE_API_KEY | Firebase public Android API key |
| Variable | FIREBASE_PROJECT_ID | Firebase project ID |
| Variable | FIREBASE_SENDER_ID | Firebase sender/project number |

Grant the Supabase token only the database/migration and edge-function deployment access needed for this project; do not select organisation-wide full access. Create a GitHub `production` environment with required reviewers. Database deployment is manual, with an explicit migration-history confirmation; normal pushes run Android and edge-function checks only.

Inspect Android verification, offline-storage-test and Edge function type checks. Download APK and test-report artifacts. A workflow file in the repository is not evidence of a successful run. Use the final branch and commit listed in the verification record; see VERIFICATION.md rather than assuming deployment follows a push.

## References

- Supabase RLS: https://supabase.com/docs/guides/database/postgres/row-level-security
- Supabase PKCE: https://supabase.com/docs/guides/auth/sessions/pkce-flow
- Native deep links: https://supabase.com/docs/guides/auth/native-mobile-deep-linking
- Edge authentication: https://supabase.com/docs/guides/functions/auth
- FCM Android: https://firebase.google.com/docs/cloud-messaging/android/get-started
- FCM HTTP v1: https://firebase.google.com/docs/cloud-messaging/send/v1-api

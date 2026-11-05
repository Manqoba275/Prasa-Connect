# PRASA Connect submission handover

Updated: 5 November 2026
Coursework: OPSC6312 POE, Group 10

## Group and responsibilities

| Student | Student number | Assigned responsibilities |
| --- | --- | --- |
| Sanele Manqoba Mazibuko | ST10444275 | Team leader and researcher; Moovit research; introduction, comparison and conclusion; report overview; final coordination and submission |
| Nonhlanhla Chirwa | ST10451192 | Researcher and planner; Gautrain research; references; REST API design; UML and data list; Gantt/project plan |
| Ramudzuli Nyito | ST10450776 | Researcher and designer; Trainline research; requirements; UI mockups; navigation diagram |

The allocation records planned ownership from the supplied role sheet. Before submission, the group should confirm that the final contribution statement reflects the work actually completed by each member.

## What changed in the app

- Replaced the earlier demo-only sign-in/data behaviour with Supabase Auth and an authenticated REST API backed by PostgreSQL policies.
- Added profile/settings persistence, timetable search, booking records with QR tickets, incident submission/history, and optional private incident-photo storage.
- Added durable offline incident queuing and retry, account-scoped local data, and background favourite-route synchronisation with Firestore, including queued removals.
- Added Google sign-in using PKCE and the registered Android callback. The team reports that Google sign-in is now working; retain a real phone test recording as evidence.
- Added Firebase Cloud Messaging registration and protected alert-sender integration. Actual notification delivery still needs a recorded phone test.
- Added English, Afrikaans and isiZulu interface labels, password visibility controls, PRASA logo/map assets, and updated app styling/navigation.
- Added Firestore security rules, Supabase migrations/functions, GitHub Actions build/test workflows, deployment notes, audit and release documentation.

The app is an academic demonstration, not an official PRASA service. Schedules and fares are sample data; bookings are not valid travel tickets. There is no payment processing, official live train feed, or live GPS train tracking.

## Evidence and verification

- Android debug build and unit tests passed in the current worktree before packaging; rerun the commands below against the final commit and keep the GitHub Actions run URL.
- CI and earlier emulator matrix results are recorded in [VERIFICATION.md](VERIFICATION.md). They verify the revisions and checks named there, not every later provider change.
- Supabase migrations/functions and Firebase Firestore rules have deployment records in [DEPLOYMENT.md](DEPLOYMENT.md); phone-side two-account isolation, route sync and notification delivery need fresh evidence.
- The PDF walkthrough is a recording plan, not a video: `output/pdf/PRASA_Connect_Video_Walkthrough.pdf`.
- Add the actual video recording and any required phone screenshots before creating the final ZIP. Do not describe mockups as live-device evidence.

## Login for assessor

Use the group-provided assessment account email. The password is intentionally not stored in this repository, submission document, or ZIP. Share the password with the assessor through an approved private channel and rotate it after assessment. Do not use a personal account as a public demo credential.

## Final checks

1. Confirm all three group members have reviewed their contribution descriptions and the AI-use statement.
2. In Android Studio or PowerShell, run `./gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon` (PowerShell: `.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon`).
3. Install the resulting debug APK on the test phone. Demonstrate account registration/login/logout, booking and QR, report submission, offline queue then reconnect, language change, map, and route sync. Demonstrate Google SSO and push notification only if the current phone setup supports them.
4. Capture genuine screenshots/video, showing errors and successful states honestly. Avoid recording passwords, tokens, personal notifications or private account data.
5. Add the actual video file to the final private submission bundle. Follow the lecturer's portal requirements for whether the video is uploaded directly or shared by an accessible link; a link must be permission-tested.
6. Verify the final GitHub branch, commit and Actions run, then create the ZIP without `.git`, local secrets, Gradle caches, temporary logs, or machine-specific configuration.
7. Review the existing Part 1 and Part 2 report PDFs/DOCX for stale statements before submitting; some were drafted before the hosted integration and language changes. Submit only the final versions requested by the lecturer.

## Rebuild commands

```powershell
.\gradlew.bat :app\testDebugUnitTest :app\lintDebug :app\assembleDebug --no-daemon
.\gradlew.bat :app\connectedDebugAndroidTest --no-daemon
```

The second command requires a running emulator or connected device. APK output: `app/build/outputs/apk/debug/app-debug.apk`. This is a debug/test APK, not a signed publication release.

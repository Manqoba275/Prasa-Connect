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

## POE gap check

| POE item | Current state | Required before hand-in |
| --- | --- | --- |
| Part 1 Research Report PDF | The repo has a combined draft DOCX, not a final research-report PDF | Finalise research, verify sourced claims/references, export and visually check PDF |
| Part 1 Planning and Design PDF | Included in the same mixed DOCX draft with post-design notes and unresolved clarification text | Produce the required standalone/combined PDF format confirmed by lecturer; the design document must be 1,000-1,500 words and include every screen purpose/mockup, navigation diagram, API/database design, UML, typed data inventory, detailed Gantt plan, introduction, conclusion and references |
| Part 2 working app/video | App source is on GitHub; debug build/unit tests pass. Video does not exist yet | Record on a physical phone with voice-over; show register/login/logout, settings, API/auth/database evidence, invalid input and all selected app features. Add the real video URL to README |
| Part 2 code and GitHub | Source, README, Actions and multiple commits are pushed on `submission-ready` | Confirm CI succeeds for this branch and submit GitHub link. Do not upload a ZIP as source: the POE explicitly disallows ZIP files |
| Part 2 comments/logging | Source contains explanatory comments and request logging | Ensure students can explain the code and logs; keep credentials, tokens and report contents out of logs/video |
| Part 3 SSO | Google sign-in reported working after setup | Demonstrate successful sign-in and logout on the final phone build |
| Part 3 offline sync | Offline report queue and background route sync implemented | Demonstrate airplane mode, queued action, restart/reconnect, exactly-once sync and cross-device/account isolation |
| Part 3 real-time push | FCM infrastructure configured | Demonstrate a real foreground/background alert and permissions/settings behavior on phone |
| Part 3 language support | English, Afrikaans, isiZulu labels implemented | Show language change affecting screens; get fluent-speaker review and identify untranslated content |
| Part 3 user features | Map, timetable, bookings/QR, reports, alerts, stations and route favourites implemented | Match the final feature list against Part 1 proposal and demonstrate the selected features, stating that live PRASA data/payment are not connected |
| Part 3 publication prep | Version/signing configuration and publication checklist exist; only a debug APK currently verified | Build and verify a signed APK/AAB; capture app screenshots and Google Play Console upload/listing evidence if applicable. A debug APK is not publication evidence |
| AI-use statement | Draft under `docs/AI_USE.md` | Group reviews/edits it to reflect actual AI use and citation requirements; under 500 words |
| Release notes | Draft under `docs/RELEASE_NOTES.md` | Group review and tie changes to the prototype baseline |
| Final package | Current ZIP is only a local convenience bundle and lacks the video/final PDFs | Do not submit it as the POE source. Submit PDFs, GitHub repo and video in the format required by the portal |

The current combined Part 1 DOCX is an unfinalised draft: it contains old prototype/API claims, unresolved clarification text, repeated Part 2 readiness material, and no clean separation into the two PDF deliverables required by the POE. It should not be handed in as-is. The existing Part 2 PDFs also contain placeholder video links and outdated descriptions; replace them with final versions or omit them if the portal does not ask for a separate Part 2 PDF.

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

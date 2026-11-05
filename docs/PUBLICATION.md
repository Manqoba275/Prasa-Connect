# Publication preparation - not yet published

1. Complete the acceptance checks in PART2_PART3_AUDIT.md. Resolve failed builds, lint and tests and verify on a physical phone.
2. Confirm permission to use PRASA branding. This academic application is not an official PRASA service. Confirm the final package ID before production registration.
3. Use Android Studio's Generate Signed App Bundle / APK to create and securely back up a private signing keystore. Never commit it or its passwords. Scripted builds read ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS and ANDROID_KEY_PASSWORD from the environment. Without them the release output is unsigned, not publication ready.
4. Run `gradlew.bat :app:testDebugUnitTest :app:lintRelease :app:assembleRelease :app:bundleRelease`. Verify the APK using Android SDK `apksigner verify --verbose`. Retain the keystore for future updates.
5. Capture actual release screenshots of authentication, timetable, booking/QR, incident history, offline queue, rail map, both languages and notification delivery. Mockups are not verification evidence.
6. Prepare accurate store text, support contact, hosted privacy policy, data-safety declaration, content rating and an account-deletion process. Describe actual Supabase/Firebase processing, retention and deletion. The app lacks self-service account deletion; resolve that before public release.
7. If required for submission, upload the signed artifact to the appropriate Play Console testing track and capture genuine upload evidence. Check current Play requirements first. An APK alone is not publication evidence.
8. Record CI run links and the verified commit SHA, update release notes, verify the supplied narrated-video link against the POE, then create the final submission tag.

Draft short description: Academic rail journey planning, bookings and incident reporting.

Privacy inventory: email/name/mobile/Auth identity; booking and incident contents; FCM token/preferences; encrypted on-device session; account-partitioned cache and unsent reports. Logs must exclude passwords, tokens, incident text and service-account keys. Define retention and support/deletion contacts before public release.

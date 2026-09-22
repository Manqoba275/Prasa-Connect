# PRASA Connect - OPSC6312 POE

Tagline: Your journey. Connected.

PRASA Connect is an Android app for PRASA commuters. It supports registration, login/logout, train timetable searching, bookings, QR-style tickets, station information, incident reporting, settings, alerts and PRASA rail map access.

## Group 10

- Sanele Manqoba Mazibuko (ST10444275)
- Nonhlanhla Chirwa (ST10451192)
- Ramudzuli Nyito (ST10450776)

## Demo Login

- Email: `nonhlanhla@prasa.demo`
- Password: `password123`

You can also create a new user with the Register screen.

## Implemented App Features

- Register with validation.
- Login with validation.
- Logout from the top bar.
- Password hashing in the app data layer.
- Timetable search.
- Ticket booking from train schedules.
- QR-style ticket display.
- Incident report submission.
- Settings for language, notifications and offline sync.
- Alerts screen.
- API/Data evidence screen with hosted Supabase request status.
- PRASA Gauteng RailMap screen.

## Supabase API and Database

Supabase project: `Prasa-Connect`

Hosted API:

```text
https://awbgflgcqjofsnagkzou.supabase.co/functions/v1/prasa-api
```

Database tables:

- `app_users`
- `schedules`
- `tickets`
- `incidents`

API routes:

- `GET /`
- `POST /auth/register`
- `POST /auth/login`
- `GET /schedules?from=Cape Town&to=Bellville`
- `POST /bookings`
- `GET /users/{userId}/tickets`
- `POST /incidents`
- `PATCH /users/{userId}/settings`

The Android app calls the hosted Supabase Edge Function for login, registration, timetable search, bookings, tickets, incident reports and settings updates. It also keeps a local fallback data layer so the classroom demo still works if the network is unavailable.

## GitHub Actions

Included workflows:

- `.github/workflows/android-build.yml` builds the Android app and runs unit tests.
- `.github/workflows/supabase-deploy.yml` deploys Supabase migrations and the Edge Function.

For the Supabase deployment workflow, add these GitHub repository secrets:

- `SUPABASE_ACCESS_TOKEN`
- `SUPABASE_DB_PASSWORD`

## Running the Project

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the app on an emulator or physical Android device.
4. Use the demo login or register a new user.

## Local Validation

```powershell
.\gradlew.bat assembleDebug --no-daemon --console=plain
```

## Recommended Demo Video Path

1. Show Supabase project tables.
2. Open the hosted API URL in a browser.
3. Register a user in the app and show the Supabase response status.
4. Log out.
5. Log in.
6. Search timetable data.
7. Make a booking.
8. Show the generated ticket from the hosted API.
9. Submit an incident report through the hosted API.
10. Open Settings and save changes.
11. Open API/Data screen.
12. Show GitHub Actions workflow files.

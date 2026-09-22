# PRASA Connect - Functional Prototype Recovery

This version addresses the grading feedback that the app could not register, log in, log out, use data, make bookings, or show API interaction.

## Demo Login

- Email: `nonhlanhla@prasa.demo`
- Password: `password123`

You can also create a new account on the Register screen.

## Working Prototype Features

- Register with validation.
- Password stored as a SHA-256 hash in the prototype data layer.
- Login with validation and error messages.
- Demo Google SSO button.
- Logout from the top bar.
- Search timetable data.
- Make a booking from a selected train schedule.
- Generate and display QR-style tickets.
- View user tickets.
- Submit incident reports.
- Change settings: language, notifications and offline sync.
- View an API/Data screen showing request/response-style logs and stored counts.
- View PRASA stations and Gauteng RailMap.

## API Note

This prototype uses a local `PrasaApi` repository inside the Android app to simulate REST API calls:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/schedules`
- `POST /api/bookings`
- `POST /api/incidents`
- `PATCH /api/users/{id}/settings`

For the final POE version, replace this local repository with a hosted REST API such as Node.js/Express on Render and a database such as PostgreSQL or Firebase/Firestore.

## Recommended Demo Path

1. Open app.
2. Register a new user.
3. Log out.
4. Log in with the new user or the demo user.
5. Open Timetable and search.
6. Book a train.
7. Open Tickets and show the generated ticket.
8. Submit an incident report.
9. Open Settings and change language/offline sync.
10. Open API screen and show stored ticket/incident counts and the last request log.
11. Log out from the top bar.

## Important

The app now compiles with:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain
```

It is still a prototype. Hosted backend integration, GitHub Actions, and full unit tests should be added before final submission.

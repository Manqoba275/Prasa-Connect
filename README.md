# PRASA Connect — OPSC6312 POE

## Group 10:
- Sanele Manqoba Mazibuko (ST10444275)
- Nonhlanhla Chirwa (ST10451192)
- Ramudzuli Nyito (ST10450776)

## What this app does
Tagline: Your journey. Connected.

PRASA Connect is a mobile application designed for the Passenger Rail Agency of South Africa (PRASA). It helps commuters plan train trips, view live train times, get delay alerts, and report incidents. The app supports multi-language usage (English, Venda, and isiZulu) and provides offline functionality for key features like timetables and favorites.

## Features implemented in Formative 02 Part 02
- User Management: Register (encrypted), Login/Logout, and Profile management.
- Journey Planning: Station selection, route viewing, and favorites (from Part 01).
- Live Train Tracking: Visual train location and status on a map (from Part 01).
- Incident Reporting: Report issues (cable theft, vandalism) with photos and timestamps.
- Offline Mode: Saved timetables and favorites accessible without internet.
- Multi-language Support: English, isiZulu, and Venda localization.
- Service Alerts: Push notifications for delays and disruptions.
- REST API: Connected to a hosted Node.js backend and PostgreSQL database.

## Tech stack
- Frontend: Android Studio, Kotlin, Jetpack Compose / Material 3
- Networking: Retrofit + GSON
- Backend: Node.js + Express.js
- Authentication: Firebase Authentication (SSO support)
- Push Notifications: Firebase Cloud Messaging (FCM)
- Database: PostgreSQL

## API & hosting
- API base URL: <Link to Render/Heroku Hosted API>
- Hosting provider: Render / Heroku
- Database: PostgreSQL

## Changelog v1
### Added
- Implemented full design specifications from Planning & Design document.
- Added multi-language support (English, Venda, isiZulu).
- Integrated Firebase for Authentication and Cloud Messaging.
- Implemented offline sync logic for timetables.
### Updated
- Refined UI colors to PRASA Blue (#004EA8) and Gold/Yellow (#FFC928).
- Enhanced incident reporting to support photo attachments.

## Running the project
1. Clone this repo.
2. Open in Android Studio.
3. Add your google-services.json (for Firebase) to the app/ directory.
4. Run on an emulator or physical device.

## Testing
- Unit tests live in app/src/test/.
- Run locally with ./gradlew test.
- Automated on every push via GitHub Actions.

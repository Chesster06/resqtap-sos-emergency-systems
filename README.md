# ResQTap (OneTapSOS)

> **ResQTap** is a personal emergency SOS and rapid response ecosystem featuring an Android mobile application, Wear OS companion app, and Firebase real-time cloud infrastructure. Designed to provide swift, reliable assistance during critical situations.

---

## Key Features

### Emergency SOS and Alerts
- **One-Tap Instant SOS**: Trigger high-priority emergency alerts immediately with a single tap.
- **Wear OS Smartwatch Companion**: Activate SOS directly from your wrist via the companion Wear OS watch app.
- **Automated Contact Dispatch**: Instantly notify designated emergency contacts with predefined messages and alert statuses.

### Real-Time Geolocation and Navigation
- **Live GPS Tracking**: Transmit accurate live location coordinates using Google Play Services Location API.
- **Google Maps Integration**: Interactive map views to navigate to emergency incidents or pinpoint the exact location of distressed users.

### Medical Profile and ID
- **Quick-Access Medical Records**: Store vital health info (blood type, allergies, chronic conditions, emergency notes).
- **First Responder Friendly**: Allows first responders or contacts to view life-saving details during an emergency.

### Chat and Social Features
- **Real-Time Incident Chat**: Embedded messaging and communication channels during active alerts.
- **Emergency Room Hub**: Coordinate with room members, family, or emergency circles during crises.
- **QR Code Sharing**: Easily scan and connect with friends, family, or response nodes using personalized QR codes.

### Community and Incident Reporting
- **Incident Reports**: Submit and monitor incident details with attachments and descriptions.
- **Medical and Safety News**: Stay updated with verified health advisories and safety bulletins.

---

## Architecture and Tech Stack

| Component | Technology | Description |
|---|---|---|
| **Mobile Client** | Android (Java / Kotlin) | Native Android application (Min SDK 24, Target SDK 36) |
| **Wearable** | Wear OS | Smartwatch companion app for quick wrist-trigger SOS |
| **Backend and Cloud** | Firebase Platform | Authentication, Realtime Database, Cloud Storage and FCM |
| **Maps and Location** | Google Play Services | Maps SDK and Fused Location Provider |
| **Build System** | Gradle (Kotlin DSL) | Android Gradle Plugin 9.x |

---

## Project Structure

```text
ResQTap/
├── app/                        # Main Android phone application
│   └── src/main/java/com/example/resqtap/
│       ├── auth/               # User authentication and onboarding
│       ├── call/               # Emergency calling integrations
│       ├── chat/               # Live chat messaging
│       ├── contacts/           # Emergency contacts management
│       ├── friend/             # QR scanning and friends management
│       ├── home/               # Dashboard and primary interface
│       ├── map/                # Google Maps and location services
│       ├── news/               # Safety and medical news feeds
│       ├── profile/            # User and medical profile details
│       ├── report/             # Incident report submission
│       ├── room/               # Group emergency rooms
│       ├── sos/                # SOS dispatch and state machine
│       └── wear/               # Phone-to-watch synchronization
├── resqtapwatch/               # Wear OS watch companion module
├── ResQTap-Website/            # Web portal / landing platform
├── firebase-functions/         # Cloud Functions for automated tasks and cleanups
└── database.rules.json         # Firebase Realtime Database security rules
```

---

## Getting Started

### Prerequisites
- **Android Studio** (Ladybug / Meerkat or newer recommended)
- **JDK 11** or higher
- Android device or emulator running **Android 7.0 (API 24)** or higher
- (Optional) Wear OS device or emulator for testing watch capabilities

### Setup Instructions

1. **Clone or Open Project**
   Open the project folder in Android Studio.

2. **Firebase Configuration**
   - Place your `google-services.json` file inside the `app/` and `resqtapwatch/` directories.
   - Ensure Firebase Authentication, Realtime Database, and Storage are properly set up in your Firebase Console.

3. **Google Maps API Key**
   - Ensure your Google Maps API key is configured in your project's local properties or manifest.

4. **Build and Run**
   - Sync project with Gradle files: `File > Sync Project with Gradle Files`
   - Select the `app` run configuration and click **Run** on your target device.

---

## Security and Privacy

- **Database Rules**: Strict rules configured via `database.rules.json` to secure user profiles, locations, and active SOS rooms.
- **Sensitive Credentials**: Avoid committing private keystores (`*.jks`), `local.properties`, or active production API keys to public version control.

---

## License

This project was developed as an academic / Final Year Project (FYP). Distributed for educational and research purposes.

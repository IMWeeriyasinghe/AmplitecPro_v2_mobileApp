# Amplitec: Multimedia-Focused Hearing Aid Application

Amplitec is an Android application designed to enhance hearing aid functionality with additional multimedia features, including Bluetooth connectivity, battery status monitoring, volume control, and audio playback. This project integrates with ESP32 for real-time data communication and Firebase for user authentication.

## Features

- **User Authentication**:
  - Secure login and registration using Firebase Authentication.
  - Persistent login state using SharedPreferences.

- **Bluetooth Integration**:
  - Real-time battery percentage monitoring from the connected ESP32 device.
  - Volume control synced to the ESP32 via Bluetooth communication.

- **Multimedia Playback**:
  - Play, pause, and navigate through an audio playlist.
  - Manage playlist with add/remove functionality.
  - Volume control buttons for adjusting system volume.

- **Seamless Navigation**:
  - Intuitive navigation between app features.
  - Back navigation with confirmation prompts.

## Technology Stack

- **Programming Language**: Java
- **Bluetooth Communication**: Bluetooth Classic (SPP Profile)
- **Multimedia**: MediaPlayer for audio playback
- **Authentication**: Firebase Authentication
- **UI Framework**: Android Jetpack (AppCompat, RecyclerView, etc.)
- **Data Persistence**: SharedPreferences

## Prerequisites

1. **Android Studio**: Install [Android Studio](https://developer.android.com/studio) to build and run the app.
2. **Firebase Setup**:
   - Create a Firebase project.
   - Enable Email/Password authentication.
   - Download the `google-services.json` file and place it in the `app/` directory.
3. **ESP32 Configuration**:
   - Ensure the ESP32 is configured with the correct Bluetooth MAC address and UUID.
   - Program the ESP32 to send battery and volume data.

## Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/amplitec.git
   cd amplitec
Open the Project: Open the project in Android Studio.

Configure Firebase:

Add the google-services.json file to the app/ directory.
Sync the project with Gradle files.
Build and Run:

Connect your Android device or start an emulator.
Build and run the app from Android Studio.
Usage
Battery Status
Navigate to the Battery Status section.
Connect to the ESP32 to view the current battery percentage.
Volume Control
Navigate to the Volume Control section.
Adjust the volume using the SeekBar.
Volume changes are sent to the ESP32 in real-time.
Multimedia
Add songs to the playlist by selecting MP3 files from your device.
Play, pause, and navigate through the playlist.
Adjust system volume using the Volume Up/Down buttons.
File Structure
bash
Copy code
Amplitec/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/amplitec/
│   │   │   │   ├── activities/      # Contains all activities
│   │   │   │   ├── adapters/        # Adapter for RecyclerView
│   │   │   │   └── utils/           # Utility classes (e.g., Bluetooth management)
│   │   │   ├── res/                 # Layout and resource files
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json
└── README.md


Future Improvements

Add support for Bluetooth Low Energy (BLE) to reduce power consumption.
Enhance multimedia playback with album art and metadata.
Implement real-time notifications for battery and volume changes.
Refactor for MVVM architecture for better maintainability.
License
This project is licensed under the MIT License. See the LICENSE file for details.

# Clock

A lightweight, high-performance desk clock for Android, optimized for Android 12 with universal compatibility. Designed specifically for OLED and AMOLED displays with pure black backgrounds, authentic Google Sans typography, edge-to-edge scaling, and built-in anti-burn-in protection.

---

## Features

- **Dynamic Time Format**: Automatically displays 12-hour (`hh:mm:ss a`) or 24-hour (`HH:mm:ss`) format based on system settings.
- **Full Date & Day Display**: Shows the full day name (`EEEE`) and date in `dd/MM/yyyy` format.
- **True OLED / AMOLED Black (`#000000`)**: Completely shuts off unlit subpixels for maximum power efficiency and infinite contrast.
- **Active Anti-Burn-In Protection**:
  - **12-Point Orbital Pixel Shifting**: Gently translates the clock position by a few pixels in a circular pattern every minute.
  - **Micro-Intensity Modulation**: Subtly modulates text luminance between white tones (`#FFFFFF`, `#EDEDED`, `#E2E2E2`) to prevent static subpixel fatigue.
- **Pinch-to-Zoom Scaling**:
  - Starts at ~75% of the screen by default.
  - Pinch in or out to dynamically resize anywhere from 40% up until the text touches the screen edge (100%).
  - Automatically saves and restores your preferred zoom level across app launches.
- **Universal Aspect Ratio Support**: Responsive layout algorithms calculate dimensions to ensure perfect fit across 4:3, 16:9, 18:9, 19.5:9, 21:9, and foldable screens.
- **Authentic Google Sans Typography**: Bundled directly within the app assets so the font never changes regardless of manufacturer skins or custom system fonts.
- **Always Landscape**: Locked to landscape orientation with `FLAG_KEEP_SCREEN_ON` enabled for bedside stands, desks, and docks.
- **Edge-to-Edge Immersive Mode**: Seamlessly hides system status bars and navigation bars using `WindowInsetsController` on Android 12+ with legacy support for older Android versions.
- **Zero-GC & Ultra-Fast**: Pure native Android framework activity with zero heavy dependencies (no Material Components, no AppCompat). Instant launch with an APK size of only **~160 KB**.

---

## How to Use

1. **Launch the App**: Open **Clock**. It will immediately enter fullscreen landscape mode with the screen kept awake.
2. **Adjust Size**: Pinch in or out with two fingers on the screen to resize the clock. The text will stop growing once it touches the edge of the display.
3. **Change Time Format**: Change between 12-hour and 24-hour formats anytime from Android Settings (**Settings > System > Date & Time > Use 24-hour format**). The app updates automatically.

---

## Technical Specifications

| Parameter | Value |
|---|---|
| **Package Name** | `com.mmam.clock` |
| **Minimum SDK** | API 21 (Android 5.0 Lollipop) |
| **Target SDK** | API 31 (Android 12) |
| **Compile SDK** | API 34 |
| **Build System** | Gradle 8.7 / Android Gradle Plugin 8.6.1 |
| **Dependencies** | None (pure framework SDK) |
| **Output APK Size** | ~160 KB |

---

## Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK Platform 34 and Build-Tools installed

### Build Command
Run the Gradle wrapper from the project root:

**Windows (PowerShell / Command Prompt):**
```powershell
.\gradlew.bat assembleDebug
```

**Linux / macOS:**
```bash
./gradlew assembleDebug
```

The compiled APK will be located at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## Project Structure

```text
Clock/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/fonts/
│       │   ├── GoogleSans-Bold.ttf
│       │   └── GoogleSans-Medium.ttf
│       ├── java/com/mmam/clock/
│       │   └── MainActivity.java
│       └── res/
│           ├── drawable/
│           │   ├── ic_launcher.xml
│           │   └── ic_launcher_round.xml
│           ├── layout/
│           │   └── activity_main.xml
│           ├── values/
│           │   ├── colors.xml
│           │   ├── strings.xml
│           │   └── styles.xml
│           └── values-v31/
│               └── styles.xml
├── build.gradle
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── settings.gradle
└── README.md
```

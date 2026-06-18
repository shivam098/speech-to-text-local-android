# Speech To Text Local — Android

Private, **on-device** voice-to-text for Android. Tap the mic, speak, and your words
are transcribed locally using Android's `SpeechRecognizer` (offline / on-device on
Android 13+) — no third-party cloud, no API keys. A pluggable clean-up step removes
filler words and stutters, fixes capitalisation and spacing, and you can copy the
result anywhere.

## Stack
- Kotlin + Jetpack Compose (Material 3)
- `android.speech.SpeechRecognizer` with on-device recognition (API 33+) and
  `EXTRA_PREFER_OFFLINE`
- Gradle (Kotlin DSL), AGP 8.7.2, Kotlin 2.0.21
- compileSdk 35 · minSdk 26 · targetSdk 35 · Java 17

## Requirements
- Android Studio (Ladybug or newer) **or** a JDK 17 + the Android SDK
- A device/emulator with a speech-recognition service (on-device works best on a
  physical Android 13+ device)

## Build & run
Open the folder in Android Studio and press **Run**, or from the command line:

```bash
# Debug APK
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug
```

> The Gradle wrapper is committed, so the correct Gradle version is fetched
> automatically on first run.

## Permissions
The app requests the **microphone** (`RECORD_AUDIO`) permission at runtime the first
time you tap the mic.

## Project layout
```
app/src/main/java/com/speechtotextlocal/app/
├── MainActivity.kt          Compose entry point
├── DictationViewModel.kt    Observable dictation state
├── speech/                  SpeechRecognizerManager (offline-first)
├── enhance/                 TextEnhancer + RuleBasedTextEnhancer
└── ui/                      DictationScreen + theme
app/src/test/                Unit tests for the text enhancer
```

## Tests
```bash
./gradlew test
```

## Roadmap
- Continuous dictation (auto-restart across pauses)
- On-device LLM enhancer for smart formatting, list detection and "speak to edit"
- Multi-language switching

---
Part of the **Speech To Text Local** family: iOS · macOS · Android.

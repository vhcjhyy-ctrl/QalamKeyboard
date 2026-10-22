# Qalam Keyboard
Native Android keyboard starter in Kotlin.

## Cloud build
Open Actions > Build Qalam Keyboard APK > Run workflow. After success, download the QalamKeyboard-debug-apk artifact, unzip it, and install app-debug.apk.

## Limitations
Local spelling correction uses a small dictionary, not a full spell checker. Voice recognition opens Android's speech recognizer, but inserting recognized text directly into the focused field is not yet implemented. Build success and device behavior must be confirmed in Actions.

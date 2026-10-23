# Qalam Keyboard — Offline Developer Edition

Native Android input method written in Kotlin. The keyboard's spelling replacements and quick phrases run on-device; normal text input does not need a Qalam account, remote API, or cloud service.

## Current features
- Arabic and English keyboard layouts.
- Adjustable key height and high-contrast, large key labels.
- Local correction rules for common Arabic and English typing mistakes.
- Suggestion row based on a small offline word map.
- Developer symbol layout with braces, brackets, operators, quotes, comments, pipes, and common programming punctuation.
- Voice entry passes recognized text back to the keyboard's active input connection. Recognition is provided by an installed Android speech-recognition app and may use that app's own offline/online capabilities; fully offline speech recognition is not guaranteed.
- Quick phrases, punctuation, backspace, enter, and correction toggle.

## Build an APK
Open **Actions → Build Qalam Keyboard APK → Run workflow** on the branch `feature/offline-pro-developer-keyboard`. Download the `QalamKeyboard-debug-apk` artifact after the workflow succeeds, unzip it, and install `app-debug.apk`.

## Enable on Android
1. Open Qalam Keyboard.
2. Enable it in Android's keyboard/input-method settings.
3. Select Qalam Keyboard from the input-method picker inside any text field.
4. Toggle **{} Dev** for programming symbols, or **قلم / Q** to switch Arabic/English layouts.

## Scope and limitations
This is a useful local-first foundation, not yet a full language-model spell checker. It only corrects known entries and intentionally leaves unknown words, names, and source code alone. Predictive quality can be expanded by bundling a properly licensed dictionary and adding tests. Speech recognition can depend on an external Android recognizer; the keyboard itself does not send typed text to a Qalam server. Build and real-device behavior still need to be verified by CI and on the target device.

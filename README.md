# Don't Tap

A fully offline Android game built with Kotlin + Jetpack Compose. No network access,
no Firebase, no backend, no ads. All 10 levels and rule mechanics from the spec are
implemented, along with local save/progress, sound (programmatic tones, no audio
assets needed), haptics, settings, and a level-select grid.

## How to build

1. Open this folder in Android Studio (Koala/2024.1 or newer recommended).
2. Let Gradle sync — it will download the Gradle distribution and dependencies
   the first time (needs internet just for that one-time setup; the built app
   itself needs none).
3. Click Run ▶ on a device or emulator (minSdk 24 / Android 7.0+).
4. To produce an installable APK directly: Build → Build Bundle(s) / APK(s) → Build APK(s).

## Project structure

- `data/GameModels.kt` — shapes, colors, GameRule enum, GameObjectData, Round, the 10 LevelConfigs
- `engine/RoundGenerator.kt` — generates a fair round for any rule (validates exactly one forbidden object, no overlapping positions)
- `engine/GameEngine.kt` — state machine: score, lives, combo/multiplier, round progression, level complete/fail
- `data/SettingsManager.kt` — SharedPreferences-backed local save (unlocked levels, best scores, sound/vibration)
- `audio/AudioManager.kt` — ToneGenerator-based sound effects + Vibrator haptics (no audio files shipped)
- `ui/screens/` — MainMenu, LevelSelect, GameScreen (HUD, countdown, play field, results), Settings, Tutorial
- `MainActivity.kt` — wires screens together with a lightweight sealed-class navigation (no nav library needed)

## Notes on what's implemented

- All 10 levels/mechanics: color, size, shape, movement, memory (previous color), change detection,
  arrow/reverse-thinking, timing/first-object, distraction, and the final mixed-mechanic gauntlet
  (last 5 rounds of Level 10 can combine "biggest + color").
- Scoring: +100 base, speed bonus for fast taps, combo multiplier (x2 at 3, x3 at 5, x4 at 10 in a row),
  combo resets on a miss. Best score and per-level best score persist locally.
- Lives: 3 per level (5 for Level 10). Losing all lives shows TRY AGAIN with a retry option.
- Back button mid-game shows a QUIT LEVEL? confirmation instead of instantly quitting.
- First launch shows a single short tutorial screen, then drops straight into Level 1.
- Reset Progress in Settings has a confirmation dialog.
- Difficulty ramps within a level (movement speed, change-detection speed, timing stagger) as specified.

## What you'll likely want to tune

- Exact colors/typography in `ui/theme/Theme.kt`
- Object sizes/spacing in `RoundGenerator.gridPositions()` if you test on a very small phone and want bigger touch targets
- Swap the programmatic ToneGenerator beeps for real sound assets later by dropping .ogg files into `res/raw` and using SoundPool instead — the AudioManager's public API (`playTap()`, `playCorrect()`, etc.) won't need to change for callers.

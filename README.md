# RareForge Android Prototype

RareForge is a standalone offline Android card-battle sandbox. It is not a modified Clash Royale client and does not connect to Supercell services.

## Features
- 12 original prototype units, all unlocked at Level 5
- Default 2.0x multiplier for hitpoints and damage; adjustable from 1.0x to 5.0x
- Displays HP, damage, attack interval, movement speed, and elixir-like cost
- Simple local battle-power simulation
- No network permissions or third-party game assets

## Build APK
1. Open this repository in Android Studio.
2. Install Android SDK Platform 35 if prompted and allow Gradle sync.
3. Select **Build → Build APK(s)**. Output: `app/build/outputs/apk/debug/app-debug.apk`.
4. Or use GitHub Actions: open **Actions → Build RareForge APK → Run workflow**, then download the `RareForge-debug-apk` artifact from the successful run.

This is a prototype, not a full real-time tower-defense engine. It cannot change an official Clash Royale account.
# AGENTS.md

Guide for coding agents working in this repository. People start with [README.md](README.md).

## What this is

Steady Volume is a small Android TV app. It levels loudness for every app on the device by holding
one Android `DynamicsProcessing` effect on audio session 0 (the global output mix). Plain Java, no
dependencies, no network, `minSdk` 28, `targetSdk` 30.

## Build and test

- JDK 17 and Android SDK platform 34 / build-tools 34.0.0. `local.properties` (ignored) points
  `sdk.dir` at the SDK.
- Build: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
- Install on a TV with ADB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- Check the effect is attached: `adb shell dumpsys media.audio_flinger` must list one effect for
  session 0, "Dynamics Processing", `Enabled y`. Logcat tag: `SteadyVolume`.
- The real test is by ear on a TV. There is no emulator path for the audio effect.

## Rules

- Never commit device identifiers: IP addresses, serial numbers, Wi-Fi names, ADB keys.
- No new dependencies and no network permission without an issue first.
- Presets are changed in `Leveler.java`, not by adding settings screens, unless an issue decides
  otherwise.

## Agent skills

### Issue tracker

Issues live in this repo's GitHub Issues (`gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

The five default labels: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`,
`wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` at the root and ADRs in `docs/adr/`. See `docs/agents/domain.md`.

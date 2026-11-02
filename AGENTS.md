# AGENTS.md

Guide for coding agents working in this repository. People start with [README.md](README.md).

## What this is

Steady Volume is a small Android TV app. It holds one leveller, an Android `DynamicsProcessing`
effect on the system output, so every app on the TV is levelled. Plain Java, no dependencies, no
network.

Before you explore the code or name a concept, read `GLOSSARY.md` (leveller, preset, volume boost,
status) and every ADR in `docs/adr/`. Use the glossary's words in code, issues and proposals. When
your output contradicts an ADR, write it as "Contradicts ADR-000N (title), but worth reopening
because…" instead of overriding it. The multi-context layout, for when this repo grows a second
glossary: `docs/agents/domain.md`.

## Build and test

- Toolchain: what `ci.yml` installs, plus the Android SDK that `build.gradle` names.
  `local.properties` (ignored) points `sdk.dir` at it.
- Build: `./gradlew assembleDebug`. Lint runs with `warningsAsErrors`: `./gradlew lintDebug`.
- JVM tests: `./gradlew testDebugUnitTest`. A behaviour gets a JVM test when it is plain Java with
  an oracle outside the code, such as a rule in `GLOSSARY.md`. Everything that touches the leveller
  or the screen is checked by ear on a TV.
- Install on a TV with ADB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- Check the leveller is attached: `adb shell dumpsys media.audio_flinger` must list one effect for
  session 0, "Dynamics Processing", `Enabled y`. Logcat tag: `SteadyVolume`.

## Release

1. Push `main` and wait until its CI run is green: `gh run list --branch main --limit 1`.
2. Push the tag by itself: `git tag -a v<version> -m "..." && git push origin v<version>`. Pushing
   branch and tag in one command made GitHub skip both workflows once.
3. Done when `gh release view v<version>` lists `steadyvolume-<version>.apk`.

## Rules

- Device identifiers stay out of the repository: IP addresses, serial numbers, Wi-Fi names, ADB keys.
- A new dependency or a network permission needs an issue first. `testImplementation` dependencies
  are exempt: they never ship in the APK.
- Preset numbers live in the `Preset` enum in `Leveler.java` (ADR-0003). A user-facing way to change
  them starts as an issue.

## Issues and triage

- Issues and specs are this repo's GitHub Issues. Before any `gh issue` call that creates, links,
  blocks or closes, read `docs/agents/issue-tracker.md`.
- Before choosing or applying a triage label, read `docs/agents/triage-labels.md`.

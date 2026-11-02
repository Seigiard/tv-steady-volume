# Review profile: Steady Volume

## What it is

A small Android TV app, plain Java, no dependencies, no network, one maintainer, one user. It holds
one leveller, a `DynamicsProcessing` effect on the system output (audio session 0), and a settings
screen built for the D-pad. Five Java files under `app/src/main/java/org/steadyvolume/tv/`. Sideloaded
APK from GitHub Releases; no Play Store, no telemetry.

## What a real failure looks like

- The leveller silently stops levelling: a dead effect handle kept after an audio-server restart,
  a `Status` that says Running while no effect sits on session 0, a lost control that is never
  reported.
- A crash on a path the remote reaches: the foreground service started and stopped before
  `startForeground`, a callback on the wrong thread, a listener left behind by the screen.
- Sound that gets louder than before without a user action: post-gain above makeup plus a 10 dB
  volume boost, a preset mapped to the wrong numbers, the -2 dB ceiling not applied.
- The service staying foreground with a notification and no effect.
- A release that does not build or sign from a `v*` tag.

## Blast radius

One TV, one user. There is no data to lose and nothing to migrate; a bad release is replaced by the
next APK. A regression is annoying for an evening, not a production incident. Severity follows what
the user hears and whether the app recovers by itself.

## The reporting bar

Material defects only, placed on what goes wrong when the code runs on a TV. Noise here:

- Migration or backward-compatibility of stored settings. One user; a changed stored id is a
  decision, not a defect.
- Performance, memory, allocation on a five-file app.
- Style, naming taste, and Java idioms with no runtime effect.
- Anything that needs a loudness meter or a second effect library; ADR-0001 rules those out.
- Raising `targetSdk`; ADR-0002 keeps it at 30 on purpose.

Lifecycle findings about `DynamicsProcessing`, `AudioEffect` listeners and foreground services need
the AOSP behaviour named, not assumed. Say when a finding is not verified on a device; none of the
reviewers has one.

## Where the rules live

`AGENTS.md` (the agent guide; `CLAUDE.md` is a symlink to it), `GLOSSARY.md` (the words: leveller,
preset, volume boost, status, system output), `docs/adr/` (three decisions), `docs/agents/`. A
deviation from any of these is always worth reporting. Tests: JVM tests exist only for plain Java
with an oracle in `GLOSSARY.md`; everything that touches the effect or the screen is checked by ear,
so a missing unit test for lifecycle code is not a finding.

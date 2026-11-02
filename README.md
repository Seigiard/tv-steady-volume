# Steady Volume for Android TV

Keeps dialogue and explosions at a similar level, for every app on the TV at once.

The app holds one Android `DynamicsProcessing` effect on the global output mix (audio session 0),
the same place the "volume booster" apps attach their Loudness Enhancer. The numbers come from the
[steady-volume](https://github.com/olerast67/steady-volume) browser extension: a slow compressor,
a makeup gain, a -2 dB limiter and an optional "clearer speech" EQ.

## What you get

- **Off / Light / Auto / Night** presets, one tile each, picked with the D-pad.
- **Volume boost** 0..10 dB on top of the levelled sound.
- **Clearer speech**: -4 dB below 160 Hz, +4 dB at 2–4 kHz.
- **Start on boot**: a foreground service brings the effect back after a reboot.
- No network permission, no analytics, no dependencies. The APK is about 40 KB.

## Requirements

- Android TV 9 or newer (API 28), with the stock `DynamicsProcessing` effect library. Most AOSP-based
  firmware ships it; check with `adb shell ls /vendor/lib/soundfx/ /system/lib/soundfx/` for
  `libdynproc.so`.
- Audio effects on session 0 need the `MODIFY_AUDIO_SETTINGS` permission only, which the app declares.

## Install

1. Download `steadyvolume-<version>.apk` from the
   [latest release](https://github.com/Seigiard/tv-steady-volume/releases/latest).
2. Put it on the TV: `adb install -r steadyvolume-<version>.apk`, or open the file from a USB stick
   with any file manager that can install APKs.
3. Open Steady Volume once, pick a preset. It starts by itself after every reboot from then on.

For updates without a computer, add this repository to
[Obtainium](https://github.com/ImranR98/Obtainium) as a GitHub source. Every release ships one
universal APK, signed with the same key, so updates install over the previous version.

## Build from source

JDK 17 and the Android SDK (platform 34, build-tools 34.0.0):

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Check that the effect is attached:

```sh
adb shell dumpsys media.audio_flinger | grep -A6 "effects for session 0"
```

## How the presets map

| Preset | Compressor threshold | Ratio | Makeup gain |
|---|---|---|---|
| Light | -26 dB | 2:1 | +6 dB |
| Auto | -32 dB | 3:1 | +12 dB |
| Night | -40 dB | 6:1 | +20 dB |

Compressor attack 300 ms, release 2 s, knee 6 dB. Limiter: -2 dB ceiling, 1 ms attack, 60 ms
release, 10:1. "Volume boost" adds to the makeup gain.

## Known limits

- Android marks effects on session 0 as deprecated. They still work on Android 9–14; a firmware
  may drop them one day.
- Apps that play through their own audio session with `AudioAttributes` flagged as not affecting
  the mix are not touched. Bluetooth offload paths may bypass the effect too.

## Licence

MIT. See [LICENSE](LICENSE).

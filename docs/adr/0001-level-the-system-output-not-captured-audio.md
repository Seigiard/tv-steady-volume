---
status: accepted
---
# Level the system output with a global effect, not captured audio

Two ways exist for a non-root app to process every other app's sound on Android TV: capture it with
AudioPlaybackCapture and play a processed copy (what RootlessJamesDSP does), or attach an audio
effect to the system output (audio session 0, what "volume booster" apps do). We attach a
`DynamicsProcessing` effect to the system output.

## Considered options

- **AudioPlaybackCapture.** Full control over the DSP, but Android 11 stops the capture a few seconds
  after the capturing app's activity leaves the foreground unless its foreground service declares the
  microphone type, which TV firmware does not grant. Tested on an Allwinner Android 11 projector:
  sound for about two seconds, then silence. Each app can also opt out of capture.
- **Effect on the system output.** Android has marked session-0 effects as deprecated since API 28,
  but they work on Android 9 through 14, need only the normal `MODIFY_AUDIO_SETTINGS` permission, and no
  app can opt out. The DSP is limited to what the firmware's `DynamicsProcessing` library offers: a
  per-band compressor, EQ and limiter, with no loudness (LUFS) measurement.

## Consequences

- The app does no audio processing of its own; it configures the firmware's effect.
- Settings that need a loudness meter (target LUFS, "raise quiet parts by up to") cannot be offered.
  Presets are expressed as compressor threshold, ratio and makeup gain instead.
- If a firmware lacks `libdynproc.so`, the app cannot work there at all.

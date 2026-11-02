---
status: accepted
---
# Render the UI in software to avoid a permanent GPU context

The process that runs the leveller lives as long as levelling is on. The first window the process
opens makes HWUI create a GPU context, and that context is not released while the process lives.
The settings screen therefore costs graphics memory and native heap even after its Activity is
finished. Software rendering avoids creating that context. The screen remains readable and the
D-pad behaviour is unchanged.

The measurements below show the cost on the TV. Finishing the Activity does not release the GPU
context.

Release 0.1.3 as installed:

| State | TOTAL PSS | Graphics (EGL+GL mtrack) | Activities |
|---|---|---|---|
| Screen in background, 2 Activity objects (as found) | 31.5 MB | 12.9 MB | 2 |
| Screen in background, 1 Activity (after restart) | 27.1 MB | 8.6 MB | 1 |
| Activity finished via `am stack remove`, service only, 20 s later | 27.7 MB | 8.6 MB | 0 |

Two debug builds, with the screen opened, a preset selected, Home pressed, and the task removed:

| Build, state | TOTAL PSS | Graphics | Native heap |
|---|---|---|---|
| As is, screen in background | 36.2 MB | 8.6 MB | 6.1 MB |
| As is, service only | 35.9 MB | 8.6 MB | 5.8 MB |
| `hardwareAccelerated="false"`, screen in background | 20.0 MB | 0 | 1.7 MB |
| `hardwareAccelerated="false"`, service only | 20.0 MB | 0 | 1.7 MB |

## Considered options

- **Software rendering (chosen).** Put `android:hardwareAccelerated="false"` on `<application>`.
  Activity windows that leave the flag alone inherit the rule, so the leveller's process does not
  create a GPU context for the settings screen or other such Activity windows.
- **Separate `:ui` process.** Rejected for now. It would need a cross-process channel for `Status`,
  while the application-level attribute already reaches the goal.

## Consequences

- The attribute lives on `<application>`, not on `MainActivity`, so Activity windows that leave the
  flag alone inherit software rendering.
- An Activity that needs GPU rendering must set `android:hardwareAccelerated="true"` on itself
  explicitly.
- Any hardware-accelerated window in this process brings the permanent GPU context back for the
  life of the process. This includes an Activity that takes the explicit exception above and a
  window that sets `FLAG_HARDWARE_ACCELERATED` itself. AOSP's
  `VoiceInteractionSession.ensureWindowCreated()` does the latter, and that session window is the
  fallback route named in issue #5. Measure such a window with the same `dumpsys meminfo` procedure
  before choosing it, or run it in a separate `:ui` process, the rejected option above.
- The settings screen costs no graphics memory in the leveller's process. The remaining process
  memory is the price of the ART process, framework code, binder, and foreground service.

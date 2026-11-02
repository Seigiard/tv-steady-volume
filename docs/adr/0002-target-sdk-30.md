---
status: accepted
---
# Keep targetSdk at 30

The app runs a plain foreground service that only holds an audio effect. From targetSdk 34 Android
requires every foreground service to declare a type, and the only type that fits is `specialUse`,
which adds a permission, a Play Console justification, and per-version behaviour changes, for no
benefit on the TVs this app is for. We stay at targetSdk 30 until a feature needs a newer target.
Sideloaded and F-Droid builds have no targetSdk floor; Google Play would require a raise before a
Play release, which is a separate decision.

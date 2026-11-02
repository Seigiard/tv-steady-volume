# Steady Volume

An Android TV app that keeps the loudness of everything the TV plays within a narrow range, so
dialogue and loud effects sit at a similar level without touching the remote.

## Language

**Levelling**:
Narrowing the loudness range of the sound: quiet passages come up, loud passages come down.
_Avoid_: normalising, boosting, compressing (as a user-facing word)

**Leveller**:
The one processing chain that performs levelling for the whole TV. There is exactly one, or none.
_Avoid_: effect, DSP, booster

**Preset**:
A named strength of levelling. One of Off, Light, Auto, Night. Exactly one preset is active.
_Avoid_: mode, profile, level

**Off**:
The preset in which the leveller is absent and sound passes through untouched.

**Light**:
The gentlest preset. Keeps most of the dynamics, evens out the extremes.
_Avoid_: Soft

**Auto**:
The default preset. Dialogue and action at a similar level; meant for films, series and YouTube.

**Night**:
The strongest preset. For a quiet room late in the evening, with no loud surprises.

**Volume boost**:
Extra loudness the user adds on top of levelled sound, from 0 to 10 dB.
_Avoid_: gain, volume, extra volume, makeup

**Makeup gain**:
The loudness a preset adds back after it has brought loud passages down. Part of the preset, not a
user setting.

**Ceiling**:
The level no sound is allowed to exceed after levelling, fixed at -2 dB below full scale.
_Avoid_: limiter threshold, max volume

**Clearer speech**:
An optional tone change that takes away low rumble and lifts the voice range so words carry better.
_Avoid_: speech EQ, voice mode, dialogue enhancer

**System output**:
The single mix of all apps' sound just before it leaves the TV. The leveller works there, so every
app is levelled and no app has to cooperate.
_Avoid_: session 0, global mix, output mix

**Status**:
What the leveller is doing right now: Off, Running, or Failed with the reason. Exactly one status,
owned by the module that runs the leveller; the settings screen only shows it.
_Avoid_: state, health, running flag

**Start on boot**:
The user's choice that the leveller comes back by itself after the TV restarts.
_Avoid_: autostart, persistent

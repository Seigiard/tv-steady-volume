package org.steadyvolume.tv;

import android.content.Context;
import android.content.SharedPreferences;

import org.steadyvolume.tv.Leveler.Preset;

/** The four user choices, stored in SharedPreferences. */
final class Settings {
    static final int GAIN_MAX_DB = 10;

    private static final String FILE = "steadyvolume";
    private static final String KEY_PRESET = "preset";
    private static final String KEY_SPEECH = "speech";
    private static final String KEY_GAIN = "gain";
    private static final String KEY_BOOT = "boot";

    private final SharedPreferences prefs;

    Settings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    Preset preset() {
        return Preset.of(prefs.getString(KEY_PRESET, null));
    }

    boolean speech() {
        return prefs.getBoolean(KEY_SPEECH, true);
    }

    /** Volume boost in dB, 0..GAIN_MAX_DB. */
    int gainDb() {
        return clampGain(prefs.getInt(KEY_GAIN, 6));
    }

    boolean startOnBoot() {
        return prefs.getBoolean(KEY_BOOT, true);
    }

    void setPreset(Preset preset) {
        prefs.edit().putString(KEY_PRESET, preset.id).apply();
    }

    void setSpeech(boolean on) {
        prefs.edit().putBoolean(KEY_SPEECH, on).apply();
    }

    void setStartOnBoot(boolean on) {
        prefs.edit().putBoolean(KEY_BOOT, on).apply();
    }

    void setGainDb(int db) {
        prefs.edit().putInt(KEY_GAIN, clampGain(db)).apply();
    }

    static int clampGain(int db) {
        return Math.max(0, Math.min(GAIN_MAX_DB, db));
    }
}

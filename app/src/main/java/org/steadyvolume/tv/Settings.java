package org.steadyvolume.tv;

import android.content.Context;
import android.content.SharedPreferences;

/** The three user choices, stored in SharedPreferences. */
final class Settings {
    static final String PRESET_OFF = "off";
    static final String PRESET_SOFT = "soft";
    static final String PRESET_AUTO = "auto";
    static final String PRESET_NIGHT = "night";

    static final int GAIN_MAX_DB = 15;

    private static final String FILE = "steadyvolume";
    private static final String KEY_PRESET = "preset";
    private static final String KEY_SPEECH = "speech";
    private static final String KEY_GAIN = "gain";
    private static final String KEY_BOOT = "boot";

    private final SharedPreferences prefs;

    Settings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    String preset() {
        return prefs.getString(KEY_PRESET, PRESET_AUTO);
    }

    boolean speech() {
        return prefs.getBoolean(KEY_SPEECH, true);
    }

    /** Extra output gain in dB, 0..GAIN_MAX_DB. */
    int gainDb() {
        int g = prefs.getInt(KEY_GAIN, 6);
        return Math.max(0, Math.min(GAIN_MAX_DB, g));
    }

    boolean startOnBoot() {
        return prefs.getBoolean(KEY_BOOT, true);
    }

    boolean enabled() {
        return !PRESET_OFF.equals(preset());
    }

    void setPreset(String preset) {
        prefs.edit().putString(KEY_PRESET, preset).apply();
    }

    void setSpeech(boolean on) {
        prefs.edit().putBoolean(KEY_SPEECH, on).apply();
    }

    void setStartOnBoot(boolean on) {
        prefs.edit().putBoolean(KEY_BOOT, on).apply();
    }

    void setGainDb(int db) {
        prefs.edit().putInt(KEY_GAIN, db).apply();
    }
}

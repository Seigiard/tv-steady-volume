package org.steadyvolume.tv;

import android.media.audiofx.AudioEffect;
import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.DynamicsProcessing.Config;
import android.media.audiofx.DynamicsProcessing.Eq;
import android.media.audiofx.DynamicsProcessing.EqBand;
import android.media.audiofx.DynamicsProcessing.Limiter;
import android.media.audiofx.DynamicsProcessing.Mbc;
import android.media.audiofx.DynamicsProcessing.MbcBand;
import android.util.Log;

/**
 * One DynamicsProcessing effect on the global output mix (audio session 0).
 *
 * The numbers follow the steady-volume browser extension: a slow compressor
 * that brings speech and effects to one level, a makeup gain, a fast limiter
 * at -2 dB, and an optional "clearer speech" EQ.
 */
final class Leveler {
    private static final String TAG = "SteadyVolume";

    private static final int GLOBAL_SESSION = 0;
    private static final int PRIORITY = 0;

    // Pre-EQ: four bands, each cutoff is the top of its band.
    private static final float[] EQ_TOP_HZ = {160f, 2000f, 4000f, 20000f};
    private static final float[] EQ_SPEECH_DB = {-4f, 0f, 4f, 0f};

    // Compressor: steady-volume measures loudness over 400 ms and lets the
    // gain fall in 0.2-0.5 s and rise in 1.5-3 s.
    private static final float MBC_ATTACK_MS = 300f;
    private static final float MBC_RELEASE_MS = 2000f;
    private static final float MBC_KNEE_DB = 6f;
    private static final float MBC_GATE_DB = -55f;

    private static final float LIMITER_ATTACK_MS = 1f;
    private static final float LIMITER_RELEASE_MS = 60f;
    private static final float LIMITER_RATIO = 10f;
    private static final float LIMITER_THRESHOLD_DB = -2f;

    /**
     * A named strength of levelling. Exactly one is active. Each preset carries the id it is
     * stored under, where the compressor starts, how hard, how much makeup, and the text the
     * settings screen shows under its tile.
     */
    enum Preset {
        OFF("off", 0f, 1f, 0f, R.string.preset_off_hint),
        LIGHT("light", -26f, 2f, 6f, R.string.preset_light_hint),
        AUTO("auto", -32f, 3f, 12f, R.string.preset_auto_hint),
        NIGHT("night", -40f, 6f, 20f, R.string.preset_night_hint);

        /** The id this preset is stored under. */
        final String id;
        final float thresholdDb;
        final float ratio;
        final float makeupDb;
        final int hintRes;

        Preset(String id, float thresholdDb, float ratio, float makeupDb, int hintRes) {
            this.id = id;
            this.thresholdDb = thresholdDb;
            this.ratio = ratio;
            this.makeupDb = makeupDb;
            this.hintRes = hintRes;
        }

        /** The preset in which the leveller is absent and sound passes through untouched. */
        boolean isOff() {
            return this == OFF;
        }

        /** The preset stored under {@code id}; a missing or unknown id is the default, Auto. */
        static Preset of(String id) {
            for (Preset p : values()) {
                if (p.id.equals(id)) {
                    return p;
                }
            }
            return AUTO;
        }
    }

    private DynamicsProcessing effect;

    /** Creates the effect if needed and pushes the current settings into it. */
    synchronized void apply(Settings s) throws IllegalStateException {
        Preset p = s.preset();
        boolean speech = s.speech();
        float postGain = p.makeupDb + s.gainDb();

        if (effect == null) {
            effect = new DynamicsProcessing(PRIORITY, GLOBAL_SESSION, buildConfig(p, speech, postGain));
            Log.i(TAG, "effect created, id=" + effect.getId());
        } else {
            effect.setPreEqAllChannelsTo(eq(speech));
            effect.setMbcAllChannelsTo(mbc(p, postGain));
            effect.setLimiterAllChannelsTo(limiter());
        }
        int rc = effect.setEnabled(true);
        if (rc != AudioEffect.SUCCESS) {
            release();
            throw new IllegalStateException("setEnabled failed: " + rc);
        }
        Log.i(TAG, "applied preset=" + p + " speech=" + speech + " postGain=" + postGain);
    }

    synchronized boolean isRunning() {
        try {
            return effect != null && effect.getEnabled();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    synchronized void release() {
        if (effect != null) {
            try {
                effect.setEnabled(false);
            } catch (IllegalStateException ignored) {
                // The effect is already gone.
            }
            effect.release();
            effect = null;
            Log.i(TAG, "effect released");
        }
    }

    private static Config buildConfig(Preset p, boolean speech, float postGain) {
        Config.Builder b = new Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                2 /* replaced by the real channel count on attach */,
                true, EQ_TOP_HZ.length,
                true, 1,
                false, 0,
                true);
        b.setPreEqAllChannelsTo(eq(speech));
        b.setMbcAllChannelsTo(mbc(p, postGain));
        b.setLimiterAllChannelsTo(limiter());
        return b.build();
    }

    private static Eq eq(boolean speech) {
        Eq eq = new Eq(true, speech, EQ_TOP_HZ.length);
        for (int i = 0; i < EQ_TOP_HZ.length; i++) {
            eq.setBand(i, new EqBand(true, EQ_TOP_HZ[i], speech ? EQ_SPEECH_DB[i] : 0f));
        }
        return eq;
    }

    private static Mbc mbc(Preset p, float postGain) {
        Mbc mbc = new Mbc(true, true, 1);
        mbc.setBand(0, new MbcBand(true, 20000f,
                MBC_ATTACK_MS, MBC_RELEASE_MS,
                p.ratio, p.thresholdDb, MBC_KNEE_DB,
                MBC_GATE_DB, 1f /* no expansion */,
                0f, postGain));
        return mbc;
    }

    private static Limiter limiter() {
        return new Limiter(true, true, 0,
                LIMITER_ATTACK_MS, LIMITER_RELEASE_MS,
                LIMITER_RATIO, LIMITER_THRESHOLD_DB, 0f);
    }
}

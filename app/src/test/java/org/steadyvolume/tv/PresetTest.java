package org.steadyvolume.tv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.steadyvolume.tv.Leveler.Preset;

public class PresetTest {
    @Test
    public void eachStoredIdResolvesToItsPreset() {
        // #given the ids the settings file stores
        // #when / #then
        assertEquals(Preset.OFF, Preset.of("off"));
        assertEquals(Preset.LIGHT, Preset.of("light"));
        assertEquals(Preset.AUTO, Preset.of("auto"));
        assertEquals(Preset.NIGHT, Preset.of("night"));
    }

    @Test
    public void missingIdIsAuto() {
        // #given a settings file with no preset yet
        // #when
        Preset p = Preset.of(null);
        // #then the glossary names Auto as the default
        assertEquals(Preset.AUTO, p);
    }

    @Test
    public void unknownIdIsAuto() {
        // #given an id no preset owns
        // #when
        Preset p = Preset.of("soft");
        // #then
        assertEquals(Preset.AUTO, p);
    }

    @Test
    public void onlyOffIsOff() {
        // #given / #when / #then
        assertTrue(Preset.OFF.isOff());
        for (Preset p : Preset.values()) {
            if (p != Preset.OFF) {
                assertEquals(p + " is not Off", false, p.isOff());
            }
        }
    }

    @Test
    public void nightIsStrongerThanAutoIsStrongerThanLight() {
        // #given the glossary order: Light gentlest, Night strongest
        // #when / #then a stronger preset starts lower and squeezes harder
        assertTrue(Preset.NIGHT.thresholdDb < Preset.AUTO.thresholdDb);
        assertTrue(Preset.AUTO.thresholdDb < Preset.LIGHT.thresholdDb);
        assertTrue(Preset.NIGHT.ratio > Preset.AUTO.ratio);
        assertTrue(Preset.AUTO.ratio > Preset.LIGHT.ratio);
    }
}

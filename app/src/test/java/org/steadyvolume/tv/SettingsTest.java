package org.steadyvolume.tv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SettingsTest {
    @Test
    public void volumeBoostStaysBetweenZeroAndTenDb() {
        // #given values below, inside and above the glossary's 0..10 dB range
        // #when / #then
        assertEquals(0, Settings.clampGain(-3));
        assertEquals(4, Settings.clampGain(4));
        assertEquals(10, Settings.clampGain(25));
    }
}

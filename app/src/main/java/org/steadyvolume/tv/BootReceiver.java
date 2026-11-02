package org.steadyvolume.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Brings the leveller back after a reboot when "Start on boot" is ticked and the preset is not Off. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && new Settings(context).startOnBoot()) {
            LevelerService.sync(context);
        }
    }
}

package org.steadyvolume.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Brings the leveller back when the app was not running: after a reboot (when "Start on boot" is
 * ticked) and after an update, which kills the process and would otherwise leave the sound
 * unlevelled until someone opens the app.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean boot = Intent.ACTION_BOOT_COMPLETED.equals(action) && new Settings(context).startOnBoot();
        boolean updated = Intent.ACTION_MY_PACKAGE_REPLACED.equals(action);
        if (boot || updated) {
            LevelerService.sync(context);
        }
    }
}

package org.steadyvolume.tv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/** Foreground service that owns the global effect for as long as levelling is on. */
public class LevelerService extends Service {
    private static final String TAG = "SteadyVolume";
    private static final String CHANNEL = "leveler";
    private static final int NOTIFICATION_ID = 1;

    /** Last outcome of apply(), for the settings screen. Null error means running. */
    private static volatile boolean sRunning;
    private static volatile String sError;

    static boolean isRunning() {
        return sRunning;
    }

    static String lastError() {
        return sError;
    }

    private final Leveler leveler = new Leveler();

    /** Starts or updates the service from the current settings. Stops it when the preset is Off. */
    static void sync(Context context) {
        Intent i = new Intent(context, LevelerService.class);
        if (new Settings(context).enabled()) {
            context.startForegroundService(i);
        } else {
            context.stopService(i);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Settings s = new Settings(this);
        if (!s.enabled()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        try {
            leveler.apply(s);
            sRunning = true;
            sError = null;
        } catch (RuntimeException e) {
            Log.e(TAG, "cannot apply effect", e);
            sRunning = false;
            sError = e.toString();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        leveler.release();
        sRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW));
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.notification_title))
                .setContentIntent(open)
                .setOngoing(true)
                .build();
    }
}

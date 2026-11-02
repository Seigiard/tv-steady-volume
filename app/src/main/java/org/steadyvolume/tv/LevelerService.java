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

/**
 * Foreground service that owns the leveller for as long as levelling is on, and owns the one
 * {@link Status} the settings screen shows.
 */
public class LevelerService extends Service {
    private static final String TAG = "SteadyVolume";
    private static final String CHANNEL = "leveler";
    private static final int NOTIFICATION_ID = 1;

    /** What the leveller is doing right now: Off, Running, or Failed with the reason. */
    static final class Status {
        enum Kind { OFF, RUNNING, FAILED }

        static final Status OFF = new Status(Kind.OFF, null);
        static final Status RUNNING = new Status(Kind.RUNNING, null);

        final Kind kind;
        /** Why the leveller is not on the system output; set only when {@code kind} is FAILED. */
        final String reason;

        private Status(Kind kind, String reason) {
            this.kind = kind;
            this.reason = reason;
        }

        static Status failed(String reason) {
            return new Status(Kind.FAILED, reason);
        }
    }

    interface StatusListener {
        void onStatus(Status status);
    }

    private static volatile Status sStatus = Status.OFF;
    private static StatusListener sListener;

    static Status status() {
        return sStatus;
    }

    /**
     * Main thread only. The listener hears the current status at once and every change after it;
     * null stops it. One listener: there is one settings screen.
     */
    static void setStatusListener(StatusListener listener) {
        sListener = listener;
        if (listener != null) {
            listener.onStatus(sStatus);
        }
    }

    private static void publish(Status status) {
        sStatus = status;
        if (sListener != null) {
            sListener.onStatus(status);
        }
    }

    private final Leveler leveler = new Leveler(reason -> {
        publish(Status.failed(reason));
        stopSelf();
    });

    /**
     * The one way in, from the screen, from boot and from an update: reads the settings and makes
     * the leveller match them. A stop also goes through the service, so onCreate's startForeground
     * always runs before the stop and the system never sees a started-but-not-foreground service.
     */
    static void sync(Context context) {
        boolean wanted = !new Settings(context).preset().isOff();
        if (wanted || sStatus.kind == Status.Kind.RUNNING) {
            context.startForegroundService(new Intent(context, LevelerService.class));
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
        if (s.preset().isOff()) {
            publish(Status.OFF);
            stopSelf();
            return START_NOT_STICKY;
        }
        try {
            leveler.apply(s);
            publish(Status.RUNNING);
            return START_STICKY;
        } catch (RuntimeException e) {
            Log.e(TAG, "cannot apply effect", e);
            publish(Status.failed(e.getMessage() != null ? e.getMessage() : e.toString()));
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        leveler.release();
        if (sStatus.kind == Status.Kind.RUNNING) {
            publish(Status.OFF);
        }
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

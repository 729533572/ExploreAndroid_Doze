package de.leo.android.explore_doze.tests;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import de.leo.android.explore_doze.MainActivity;
import de.leo.android.explore_doze.R;
import de.leo.android.explore_doze.util.LogActions;

/**
 * Test behaviour of a handler with a runnable rescheduled with postdelay in a service.
 *
 * Created by Matthias Leonhardt on 28.06.16.
 */
public class TaskHandlerInForegroundService2 extends TaskBase {
    private static final int ONGOING_NOTIFICATION_ID = 2;
    private static final String TAG = TaskHandlerInForegroundService2.class.getSimpleName();


    public void startTask(Context context, boolean startAction, long interval) {

        final Intent intent = new Intent(context, HandlerService.class);
        if (startAction) {
            intent.putExtra("interval", interval);
            context.startService(intent);
        } else {
            context.stopService(intent);
        }
    }


    public static class HandlerService extends Service {

        public class HandlerServiceBinder extends Binder {
            public HandlerService getService() { return HandlerService.this; }
        }


        private final IBinder binder = new HandlerServiceBinder();

        private Handler handler = null;
        private Runnable task;
        private long interval;
        private boolean isRunning = false;

        @Nullable
        @Override
        public IBinder onBind(Intent intent) { return binder; }

        public boolean isRunning() { return isRunning; }


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            final Context context = getApplicationContext();

            if (intent == null) {
                LogActions.logState(context, TAG, "onStartCommand: without intent");

            } else {
                if (intent.hasExtra("interval")) {
                    interval = intent.getLongExtra("interval", 0);
                    LogActions.logState(context, TAG, "onStartCommand: interval " + interval);
                } else
                    LogActions.logState(context, TAG, "onStartCommand: intent without interval");
            }

            if (interval > 0) {
                if (handler == null) {
                    handler = new Handler();
                    task = null;

                } else if (task != null) {
                    handler.removeCallbacks(task);
                    task = null;
                    LogActions.logState(context, TAG, interval, "onStartCommand: old task removed from handler");
                }

                TaskBase.registerDeviceStateReceiver(context, TAG);

                task = new HandlerTask(context, TAG, handler, interval);
                LogActions.logState(context, TAG, interval, "onStartCommand: task scheduled");
            } else
                LogActions.logState(context, TAG, interval, "onStartCommand: No interval defined - no task scheduled");

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

//            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new Notification.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(TAG)
                    .setOngoing(true)
                    .build();

            startForeground(ONGOING_NOTIFICATION_ID, notification);

            isRunning = true;

            return Service.START_STICKY;
        }

        @Override
        public void onDestroy() {

            if (isRunning) {
                handler.removeCallbacks(task);
                handler = null;
                task = null;

                TaskBase.unregisterDeviceStateReceiver(getApplicationContext(), TAG);
                LogActions.logState(getApplicationContext(), TAG, "onDestroy(): removed task and deleted handler");
            }

            isRunning = false;
            super.onDestroy();
        }
    }
}

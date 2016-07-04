package de.leo.android.explore_doze.tests;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import de.leo.android.explore_doze.util.LogActions;

/**
 * Test behaviour of a handler with a runnable rescheduled with postdelay in a service.
 *
 * Created by Matthias Leonhardt on 28.06.16.
 */
public class TaskHandlerInBackgroundService extends TaskBase {
    private static final String TAG = TaskHandlerInBackgroundService.class.getSimpleName();

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
        private final IBinder binder = new HandlerServiceBinder();

        private Handler handler = null;
        private Runnable task;
        private long interval;
        private boolean isRunning = false;

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }

        public boolean isRunning() { return isRunning; }

        public class HandlerServiceBinder extends Binder {
            public HandlerService getService() {
                return HandlerService.this;
            }
        }


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

package de.leo.android.explore_doze.tests;

import android.content.Context;
import android.os.Handler;

import de.leo.android.explore_doze.util.LogActions;

/**
 * Created by leo on 28.06.16.
 */
public class TaskHandlerOnMainThread extends TaskBase {
    private static final String TAG = "HandlerOnMainThread.postdelayed";

    private static Handler handler = null;
    private static Runnable task;
    private static boolean isRunning = false;

    public static boolean isRunning() { return isRunning; }

    public void startTask(Context context, boolean startAction, long interval) {

        if (startAction) {
            TaskBase.registerDeviceStateReceiver(context, TAG);

            if (handler == null) handler = new Handler();

            task = new HandlerTask(context, TAG, handler, interval);
            LogActions.logState(context, TAG, interval, "task scheduled");

        } else {
            TaskBase.unregisterDeviceStateReceiver(context, TAG);
            handler.removeCallbacks(task);
            LogActions.logState(context, TAG, interval, "task removed");
        }
    }
}

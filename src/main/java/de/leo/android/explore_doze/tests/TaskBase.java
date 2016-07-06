package de.leo.android.explore_doze.tests;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;

import de.leo.android.explore_doze.service.MonitorService;
import de.leo.android.explore_doze.util.LogActions;

/**
 * Created by leo on 28.06.16.
 */
public abstract class TaskBase {

    public abstract void startTask(Context context, boolean startAction, long interval);


    /**
     * Simple runnable that upon run posts itself delayed on the same handler and
     * logs the state of the device on every run.
     */
    protected static class HandlerTask implements Runnable {
        private Context context;
        private String tag;
        private Handler handler;
        public long interval;

        public HandlerTask(Context context, String tag, Handler handler, long interval) {
            this.context = context;
            this.tag = tag;
            this.handler = handler;
            this.interval = interval;

            this.run();
        }

        @Override
        public void run() {
            LogActions.logState(context, tag, interval, "Handlertask run");
            handler.postDelayed(this, interval);
        }
    }


    private static ArrayList<String> tags = new ArrayList<>();


    protected static void registerDeviceStateReceiver(Context context, String tag) {
        if (!tags.contains(tag)) tags.add(tag);
        context.startService(new Intent(context, MonitorService.class));
    }


    protected static void unregisterDeviceStateReceiver(Context context, String tag) {
        if (tags.contains(tag)) tags.remove(tag);

        if (tags.size() == 0) context.stopService(new Intent(context, MonitorService.class));
    }
}

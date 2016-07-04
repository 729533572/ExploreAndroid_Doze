package de.leo.android.explore_doze.tests;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Locale;

import de.leo.android.explore_doze.util.LogActions;
import de.leo.android.explore_doze.service.MonitorService;
import de.leo.android.explore_doze.util.SntpClient;

/**
 * Created by leo on 28.06.16.
 */
public abstract class TaskBase {
    private final static String NTP_SERVER = "192.168.84.1";
    private final static int TIMEOUT = 10000;

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
            TaskBase.monitorState(context, tag, interval, "Handlertask run");
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


    public static String getConnectionStatus(Context context) {
        StringBuilder status = new StringBuilder();

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) status.append("No active Network");
        else {
            status.append(networkInfo.isConnected() ? "isConnected" : "not connected");
            status.append(", ");
            status.append(networkInfo.isConnectedOrConnecting() ? "isConnectedOrConnecting" : "not isConnectedOrConnecting");

            String connectionType;
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_BLUETOOTH:
                    connectionType = "BLUETOOTH";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    connectionType = "Mobile";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    connectionType = "WIFI";
                    break;
                case ConnectivityManager.TYPE_VPN:
                    connectionType = "VPN";
                    break;
                default:
                    connectionType = "Unknown (" + networkInfo.getType() + ")";
            }
            status.append(", ").append(connectionType);
        }

        return status.toString();
    }


    public static void monitorState(Context context, String tag, long interval, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);

            // Check Network Connectivity
        SntpClient client = new SntpClient();
        if (client.requestTime(NTP_SERVER, TIMEOUT)) {
            long time1 = client.getNtpTime();
            long time2 = System.currentTimeMillis();
            sb.append(String.format(Locale.getDefault(), "\nNTP-Time %d, delta %d", time1, time1-time2));
        } else {
            sb.append("\nNTP-Request not sucessful");
        }

        sb.append("\n").append(getConnectionStatus(context));

        LogActions.logState(context, tag, interval, sb.toString());
    }

}

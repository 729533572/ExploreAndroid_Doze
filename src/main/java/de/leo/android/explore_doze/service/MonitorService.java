package de.leo.android.explore_doze.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import de.leo.android.explore_doze.util.LogActions;

/**
 * Service to keep a broadcast receiver running when the app is not active
 *
 * Created by leo on 04.07.16.
 */
public class MonitorService extends Service {
    private static final String TAG =  MonitorService.class.getSimpleName();

    //### Does not fire when the device enters or leaves Doze mode and the connectivit definitively changes
    public static class DeviceStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogActions.logState(context, TAG, intent.getAction());
        }
    }


    private DeviceStateChangedReceiver receiver = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (receiver == null) {
            receiver = new DeviceStateChangedReceiver();

            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);  //"android.net.conn.CONNECTIVITY_CHANGE"
            registerReceiver(receiver, intentFilter);

            intentFilter = new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);  //"android.os.action.DEVICE_IDLE_MODE_CHANGED"
            registerReceiver(receiver, intentFilter);

//TODO            intentFilter = new IntentFilter(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);     // -> https://code.google.com/p/android/issues/detail?id=211639
            intentFilter = new IntentFilter("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");     // Funktioniert schon
            registerReceiver(receiver, intentFilter);

            LogActions.logState(getApplicationContext(), TAG, "onStartCommand(): DeviceState-Receiver registered");
        }

        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (receiver != null) unregisterReceiver(receiver);

        LogActions.logState(getApplicationContext(), TAG, "onDestroy(): DeviceState-Receiver unregistered");
        super.onDestroy();
    }
}

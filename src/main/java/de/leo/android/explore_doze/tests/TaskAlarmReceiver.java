package de.leo.android.explore_doze.tests;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.leo.android.explore_doze.util.LogActions;

/**
 * Test behaviour of AlarmManager.setAndAllowWhileIdle()
 *
 * Created by Matthias Leonhardt on 28.06.16.
 */
public class TaskAlarmReceiver extends TaskBase {
    private static final String TAG = TaskAlarmReceiver.class.getSimpleName();

    /**
     * Broadcastreceiver to consume the alarms and to monitor the state of the device.
     */
    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogActions.logState(context, TAG, intent.getLongExtra("interval", -1L), intent.getStringExtra("msg"));
        }
    }


    public static boolean isRunning(Context context) {
        return  (PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);
    }


    @Override
    public void startTask(Context context, boolean startAction, long interval) {

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TAG", TAG);
        intent.putExtra("msg", "alarm repeating " + interval);
        intent.putExtra("interval", interval);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (startAction) {
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, interval, pendingIntent);        //API > 19: Inexact!
            TaskBase.registerDeviceStateReceiver(context, TAG);
            LogActions.logState(context, TAG, interval, "Alarm activated");
        } else {
            am.cancel(pendingIntent);
            TaskBase.unregisterDeviceStateReceiver(context, TAG);
            LogActions.logState(context, TAG, -1, "Alarm deacitvated");
        }
    }
}

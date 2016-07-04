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
public class TaskAlarmReceiverAllowWhileIdle extends TaskBase {
    private static final String TAG = TaskAlarmReceiverAllowWhileIdle.class.getSimpleName();


    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long interval = intent.getLongExtra("interval", -1L);
            TaskBase.monitorState(context, TAG, interval, intent.getStringExtra("msg") + interval);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent);
        }
    }


    public static boolean isRunning(Context context) {
        return (PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);
    }


    @Override
    public void startTask(Context context, boolean startAction, long interval) {

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TAG", TAG);
        intent.putExtra("msg", "alarm (allow while idle) repeating ");
        intent.putExtra("interval", interval);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (startAction) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent);
            TaskBase.registerDeviceStateReceiver(context, TAG);
            LogActions.logState(context, TAG, interval, "Alarm (Allow while idle) activated");
        } else {
            am.cancel(pendingIntent);
            TaskBase.unregisterDeviceStateReceiver(context, TAG);
            LogActions.logState(context, TAG, -1, "Alarm (Allow while idle) deacitvated");
        }
    }
}

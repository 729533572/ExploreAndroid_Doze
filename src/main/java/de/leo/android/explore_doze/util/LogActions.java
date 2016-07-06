package de.leo.android.explore_doze.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.leo.android.explore_doze.service.MonitorService;
import de.leo.android.explore_doze.tests.TaskAlarmReceiver;
import de.leo.android.explore_doze.tests.TaskAlarmReceiverAllowWhileIdle;
import de.leo.android.explore_doze.tests.TaskHandlerInBackgroundService;
import de.leo.android.explore_doze.tests.TaskHandlerInForegroundService;
import de.leo.android.explore_doze.tests.TaskHandlerInForegroundService2;
import de.leo.android.explore_doze.tests.TaskHandlerOnMainThread;

/**
 * Log various properties of the current system state
 *
 * Created by Matthias Leonhardt on 28.06.16.
 */
public class LogActions extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LogActions.db";
    private static final int DATABASE_VERSION = 1;

    private final static String NTP_SERVER = "192.168.84.1";    // Private NTP-Server - please dont spam public NTP-Servers with this
    private final static int NTP_TIMEOUT = 10000;

    public static final String TBL_EVENTS = "events";
    public static final String COL_EVENTS_CONNECTED = "isconnected";
    public static final String COL_EVENTS_CONNECTING = "isconnecting";
    public static final String COL_EVENTS_CONNECTIONTYPE = "connectiontype";
    public static final String COL_EVENTS_DELTA = "delta";
    public static final String COL_EVENTS_NTP_DELTA = "ntp_delta";
    public static final String COL_EVENTS_IDLE = "isidle";
    public static final String COL_EVENTS_LIGHT_IDLE = "isidle_light";
    public static final String COL_EVENTS_INTERACTIVE = "isinteractive";
    public static final String COL_EVENTS_INTERVAL = "interval";
    public static final String COL_EVENTS_MSG = "msg";
    public static final String COL_EVENTS_TAG = "tag";
    public static final String COL_EVENTS_TIME = "time";
    public static final String COL_EVENTS_TIME_STR = "time_str";


    public LogActions(Context context) {
        this(context, null);
    }


    public LogActions(Context context, String filename) {
        super(context, filename == null ? Environment.getExternalStorageDirectory() + "/" + DATABASE_NAME : filename, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TBL_EVENTS + " ("
                + COL_EVENTS_TIME + " LONG, "
                + COL_EVENTS_TIME_STR + " TEXT, "
                + COL_EVENTS_TAG + " TEXT, "
                + COL_EVENTS_INTERVAL + " LONG, "
                + COL_EVENTS_DELTA + " LONG, "
                + COL_EVENTS_LIGHT_IDLE + " INT, "
                + COL_EVENTS_IDLE + " INT, "
                + COL_EVENTS_INTERACTIVE + " INT, "
                + COL_EVENTS_CONNECTED + " INT, "
                + COL_EVENTS_CONNECTING + " INT, "
                + COL_EVENTS_NTP_DELTA + " LONG, "
                + COL_EVENTS_CONNECTIONTYPE + " TEXT, "
                + COL_EVENTS_MSG + " TEXT"
                + ")";

        sqLiteDatabase.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public static void logState(Context context, String tag, String msg) {
        logState(context, tag, -1, msg);
    }


    /**
     * Record the current state of the device to the database
     */
    public static void logState(Context context, String tag, long interval, String msg) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss", Locale.GERMAN);
        final long time = System.currentTimeMillis();

        LogActions la = new LogActions(context);
        SQLiteDatabase db = la.getWritableDatabase();
        Cursor c = db.query(TBL_EVENTS, new String[] { "max(time)" }, COL_EVENTS_TAG + "='" + tag +"'", null, null, null, null);
        assert  c != null;
        long lastEvent = c.moveToFirst() ? c.getLong(0) : time;
        c.close();

        final ContentValues v = new ContentValues();
        v.put(COL_EVENTS_TIME, time);
        v.put(COL_EVENTS_TIME_STR, sdf.format(time));
        v.put(COL_EVENTS_TAG, tag);
        v.put(COL_EVENTS_DELTA, time - lastEvent);

        if (interval >= 0) v.put(COL_EVENTS_INTERVAL, interval);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        v.put(COL_EVENTS_IDLE, pm.isDeviceIdleMode());
//TODO:     v.put(COL_EVENTS_LIGHT_IDLE, pm.isLightDeviceIdleMode());   //Hat angeblich @Hide Annotation -> https://code.google.com/p/android/issues/detail?id=211639
        v.put(COL_EVENTS_INTERACTIVE, pm.isInteractive());

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) v.put(COL_EVENTS_CONNECTIONTYPE, "No active Network");
        else {
            v.put(COL_EVENTS_CONNECTED, networkInfo.isConnected());
            v.put(COL_EVENTS_CONNECTING, networkInfo.isConnectedOrConnecting());

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
            v.put(COL_EVENTS_CONNECTIONTYPE, connectionType);

            // Check Network Connectivity
            SntpClient client = new SntpClient();
            if (client.requestTime(NTP_SERVER, NTP_TIMEOUT)) {
                long time1 = client.getNtpTime();
                long time2 = System.currentTimeMillis();
                v.put(COL_EVENTS_NTP_DELTA, time1 - time2);
                msg += String.format(Locale.getDefault(), ", NTP-Time %d, delta %d", time1, time1 - time2);
            }
        }

        v.put(COL_EVENTS_MSG, msg);

        db.insert(LogActions.TBL_EVENTS, null, v);
        db.close();

        Log.d(tag, (time - lastEvent) + " " + msg);
    }


    /**
     * Check the recorded events
     */
    public static String checkEvents(Context context, String filename) {
        ArrayList<String> messages = new ArrayList<>();

        HashMap<String, Watchdog> watchdogs = new HashMap<>();

        DeviceState deviceState = new DeviceState();

        LogActions la = new LogActions(context, filename);
        SQLiteDatabase db = la.getWritableDatabase();

        Cursor events = db.query(TBL_EVENTS, null, null, null, null, null, COL_EVENTS_TIME + ", " + COL_EVENTS_TAG);
        assert events != null;
        int colTime = events.getColumnIndexOrThrow(COL_EVENTS_TIME);
        int colTag = events.getColumnIndexOrThrow(COL_EVENTS_TAG);
        int colInterval = events.getColumnIndexOrThrow(COL_EVENTS_INTERVAL);

        messages.add("Start checking events.\n");

        String tag;
        long time;
        int interval;
        while (events.moveToNext()) {
            deviceState.updateState(events, messages);

            time = events.getLong(colTime);
            tag = events.getString(colTag);
            interval = events.getInt(colInterval);

            if (tag.equals(TaskHandlerOnMainThread.class.getSimpleName())
                    || tag.equals("HandlerOnMainThread.postdelayed")
                    || tag.equals(TaskHandlerInBackgroundService.class.getSimpleName())
                    || tag.equals(TaskHandlerInForegroundService.class.getSimpleName())
                    || tag.equals(TaskHandlerInForegroundService2.class.getSimpleName())) {
                if (!watchdogs.containsKey(tag) && interval > 0)
                    watchdogs.put(tag, new Watchdog(tag, interval, 10000, time));

            } else if (tag.equals(TaskAlarmReceiver.class.getSimpleName())
                    || tag.equals(TaskAlarmReceiverAllowWhileIdle.class.getSimpleName())) {
                if (interval > 0 && !watchdogs.containsKey(tag)) {
                    if (interval < 10000) interval = 10000;     // Current minimum, enforced by Ggoogle
                    watchdogs.put(tag, new Watchdog(tag, interval, 60000, time));
                }

            } else if (tag.equals(MonitorService.class.getSimpleName())) {
                // Nothing to do - already consumed by deviceState.updateState()

            } else {
                messages.add(messageString(events, "Unknown tag " + tag + " encountered"));
            }

            for (Watchdog watchdog : watchdogs.values())
                watchdog.check(tag, time, deviceState, messages);
        }
        events.close();

        messages.add("Finished checking events.");

        return messages.toString();
    }
/*
        StringBuilder result = new StringBuilder();
        for (String msg : messages) result.append(msg);

        return result.toString();
    }
*/

    public static String messageString(Cursor event, String msg) {
        return messageString(event.getLong(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_TIME)), event.getString(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_TAG)), msg);
    }

    public static String messageString(Long time, String tag, String msg) {
        return String.format("%s %s %s\n", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(time), tag, msg);
    }
}

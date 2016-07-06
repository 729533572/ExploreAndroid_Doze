package de.leo.android.explore_doze.util;

import android.database.Cursor;

import java.util.ArrayList;

import de.leo.android.explore_doze.service.MonitorService;

/**
 *
 * Created by leo on 06.07.16.
 */
public class DeviceState {

    private long time;
    private boolean isInteractive = false;
    private boolean isIdle = false;
    private long idleStateSince = 0;
    private boolean isIdleLight = false;
    private long idleStateLightSince = 0;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean hasNetwork = false;

    public boolean isConnected() { return isConnected; }
    public boolean isIdle() { return  isIdle; }
    public long getIdleStateSince() { return idleStateSince; }

    public void updateState(Cursor event, ArrayList<String> messages) {
        time = event.getLong(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_TIME));
        String tag = event.getString(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_TAG));
        String msg = event.getString(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_MSG));
//        int interval = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_INTERVAL));
//        int delta = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_DELTA));
        hasNetwork = !event.isNull(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_NTP_DELTA));
        isConnected = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_CONNECTED)) != 0;
        isConnecting = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_CONNECTING)) != 0;
        isInteractive = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_INTERACTIVE)) != 0;

        boolean isIdle_new = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_IDLE)) != 0;
//        boolean isIdleLight_new = event.getInt(event.getColumnIndexOrThrow(LogActions.COL_EVENTS_LIGHT_IDLE)) != 0;   // TODO: Not yet implemented by Google

        if (tag.equals(MonitorService.class.getSimpleName())) {
                // Consume events that were fired because of a change of the state of the device

            String msg1 = msg.contains("\n") ? msg.substring(0, msg.indexOf("\n")) : msg;
            switch (msg1) {
                case "android.os.action.DEVICE_IDLE_MODE_CHANGED":
                    // Unfortunately there was no information in the event that indicates from which to which state the mode changed.
                    isIdle = !isIdle;
                    idleStateSince = time;

                    if (isIdle != isIdle_new) {
                        messages.add(LogActions.messageString(event, "Device-Idle Mode has an inexpected state: current:" + isIdle_new + " expected: " + isIdle));
                        isIdle = isIdle_new;
                    }

                    if (isInteractive && !isIdle) {
                        messages.add(LogActions.messageString(event, "Device in idle mode while reported interactive"));
                        isIdle = false;
                    }
                    break;

                case "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED":
                    // Unfortunately there was no information in the event that indicates from which to which state the mode changed.
                    isIdleLight = !isIdleLight;
                    idleStateLightSince = time;

//                    if (isIdleLight != isIdleLight_new) {  // TODO: Not yet implemented by Google
//                        messages.add(messageString(event, "Device-Idle Mode has an inexpected state: current:" + isIdleLight_new + " expected: " + isIdleLight));
//                        isIdleLight = isIdleLight_new;
//                    }

                    if (isInteractive && !isIdleLight) {
                        messages.add(LogActions.messageString(event, "Device in light idle mode while reported interactive"));
                        isIdleLight = false;
                    }
                    break;

//                case "android.net.conn.CONNECTIVITY_CHANGE":
                    //TODO
//                    break;
            }
        }

        checkState(messages, tag);
    }


    /**
     * Check the current state of the device for consistency
     */
    public boolean checkState(ArrayList<String> messages, String tag) {
        boolean stateOK = true;

        if (isConnected && !hasNetwork) {
            messages.add(LogActions.messageString(time, tag, "Device reported as connected but network access failed"));
            stateOK = false;
        }

        return stateOK;
    }
}

package de.leo.android.explore_doze.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.leo.android.explore_doze.tests.TaskAlarmReceiver;
import de.leo.android.explore_doze.tests.TaskAlarmReceiverAllowWhileIdle;

/**
 * Check that the periodic tasks are executed within the expected time-intervals.
 * <p>Take into account that alarms are suspended in idle-mode and AlarmAndAllowWhileIdle is
 * throttled to a minimum interval of 9 minutes in idle-mode.</p>
 *
 * Created by Matthias Leonhardt on 29.06.16.
 */
public class Watchdog {
    /**
     * Time interval [ms] after the beginning of a maintenance window in which the tasks have to
     * be started. Otherwise they are considered tardy.
     */
    public static final long INTERVAL_MAINTENANCE_START = 10000;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private String tag;
    private long interval;
    private long allowed_deviation;
    private long lastEvent;
    private boolean wasOnTime;


    /**
     * Create a new watchdog for the thread identified by tag.
     *
     * @param tag               Tag value for the events from this thread
     * @param interval          Interval in which the events are expected to occur (unter normal conditions) [ms]
     * @param allowed_deviation Allowed deviation from the expected interval [ms]
     * @param startTime         Initial timestamp
     */
    public Watchdog(String tag, long interval, long allowed_deviation, long startTime) {
        this.tag = tag;
        this.interval = interval;
        this.allowed_deviation = allowed_deviation;
        this.lastEvent = startTime;

        wasOnTime = true;
    }


    /**
     * Check that the event corrsponding to this timer is fired in time.
     * <p>Take into account that Android defers some types of job or enlarges the minimum interval</p>
     *
     * @param event_tag     tag of the current event
     * @param time          timestamp of the current event
     * @param deviceState   Current modelled state of the device
     * @param messages      List of Strings for messages
     */
    public void check(String event_tag, long time, DeviceState deviceState, ArrayList<String> messages) {

        long currentInterval = interval;    // Interval that is adjusted by the task and the current state of the device

        if (tag.equals(TaskAlarmReceiver.class.getSimpleName())) {
            if (deviceState.isIdle())
                currentInterval = Long.MAX_VALUE;   // Alarms are suspended in idle-mode -> indefinite interval

        } else if (tag.equals(TaskAlarmReceiverAllowWhileIdle.class.getSimpleName())) {
            if (deviceState.isIdle() && interval < 9 * 60000)
                currentInterval = 9 * 60000;          // Minimum interval in idle-mode, enforced by Google
            currentInterval += allowed_deviation;

        } else
            currentInterval += allowed_deviation;

        if (wasOnTime && time - lastEvent > currentInterval) {    // Event is tardy
            // Also check if there was a change to the non-idle mode recently. This results in shorter
            // intervals, but maybe the tasks weren't started yet. Add an timespan from the change of
            // the idle-mode to allow the start of the various tasks.
            if (deviceState.isIdle() || (!deviceState.isIdle() && time - deviceState.getIdleStateSince() > INTERVAL_MAINTENANCE_START)) {
                wasOnTime = false;
                messages.add(LogActions.messageString(time, tag, sdf.format(lastEvent) + " " + (time - lastEvent) + " tardy\n"));
            }
        } else
            wasOnTime = true;

        if (event_tag.equals(tag))
            lastEvent = time;       // Record that this thread was executed now
    }
}

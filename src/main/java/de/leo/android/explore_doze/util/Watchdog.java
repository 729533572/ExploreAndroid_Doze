package de.leo.android.explore_doze.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by leo on 29.06.16.
 */
public class Watchdog {
//    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private String tag;
    private long interval;
    private long lastEvent;
    private boolean wasOnTime;

    public Watchdog(String tag, long interval, long startTime) {
        this.tag = tag;
        this.interval = 2 * interval;
        this.lastEvent = startTime;

        wasOnTime = true;
    }

    public void check(String tag, long time, StringBuilder result) {

        if (wasOnTime && time - lastEvent > interval) {
            wasOnTime = false;
            result.append(this.tag + " " + sdf.format(time) + " " + sdf.format(lastEvent) + " " + (time - lastEvent) + " tardy\n");
        } else
            wasOnTime = true;

        if (tag.equals(this.tag))
            lastEvent = time;

    }
}

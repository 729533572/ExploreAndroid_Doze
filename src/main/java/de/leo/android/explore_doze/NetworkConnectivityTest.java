package de.leo.android.explore_doze;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import de.leo.android.explore_doze.tests.TaskBase;
import de.leo.android.explore_doze.util.LogActions;

/**
 * Created by leo on 27.06.16.
 */
public class NetworkConnectivityTest {
    private static final String LOG_TAG = NetworkConnectivityTest.class.getSimpleName();
    private static final int MY_BACKGROUNDJOB = 1;


    public static class MyJobSchedulerTask extends JobService {

        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            Log.i(MyJobSchedulerTask.class.getSimpleName(), "onStartJob");
            TaskBase.getConnectionStatus(getApplicationContext());
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return false;
        }
    }

    public void JobSchedulerTest(Context context) {
        JobInfo job = new JobInfo.Builder(MY_BACKGROUNDJOB,
                new ComponentName(context, MyJobSchedulerTask.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .build();

        JobScheduler js = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.schedule(job);
        Log.i(LOG_TAG, "Job scheduled");
    }



    public static class MyGCMServiceTask extends GcmTaskService {

        @Override
        public int onRunTask(TaskParams taskParams) {
            LogActions.logState(getApplicationContext(), LOG_TAG, "connectionStatus: " + TaskBase.getConnectionStatus(getApplicationContext()));

            return GcmNetworkManager.RESULT_SUCCESS;
        }
    }

    public void GCMNetworkManagerTest(Context context) {
        OneoffTask myTask = new OneoffTask.Builder()
                .setService(MyGCMServiceTask.class)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
//                .setExecutionWindow(0, Long.MAX_VALUE)
                .setExecutionWindow(0, 1)
                .setTag("GCMNetworkManagerTest")
                .build();

        GcmNetworkManager.getInstance(context).schedule(myTask);
        LogActions.logState(context, LOG_TAG, "GCM OneOff task scheduled");
    }
}

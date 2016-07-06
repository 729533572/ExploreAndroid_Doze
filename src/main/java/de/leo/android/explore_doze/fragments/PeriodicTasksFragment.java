package de.leo.android.explore_doze.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import de.leo.android.explore_doze.R;
import de.leo.android.explore_doze.tests.TaskAlarmReceiver;
import de.leo.android.explore_doze.tests.TaskAlarmReceiverAllowWhileIdle;
import de.leo.android.explore_doze.tests.TaskHandlerInBackgroundService;
import de.leo.android.explore_doze.tests.TaskHandlerInForegroundService;
import de.leo.android.explore_doze.tests.TaskHandlerInForegroundService2;
import de.leo.android.explore_doze.tests.TaskHandlerOnMainThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class PeriodicTasksFragment extends Fragment {

    private CheckBox cbAlarmSetrepeating;
    private CheckBox cbAlarmSetAndAllowWhileIdle;
    private CheckBox cbHandlerBackgroundservice;
    private CheckBox cbHandlerForegroundservice;
    private CheckBox cbHandlerForegroundserviceOwnProcess;
    private CheckBox cbHandlerMainthread;
    private TaskHandlerInBackgroundService.HandlerService service1;
    private TaskHandlerInForegroundService.HandlerService service2;
/* TODO: Does not work with services in different processes
    private TaskHandlerInForegroundService2.HandlerService service3;
*/
    private boolean isServicing = false;

    private ServiceConnection connection1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TaskHandlerInBackgroundService.HandlerService.HandlerServiceBinder b = (TaskHandlerInBackgroundService.HandlerService.HandlerServiceBinder) iBinder;
            service1 = b.getService();
            cbHandlerBackgroundservice.setChecked(service1.isRunning());
            getActivity().unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service1 = null;
        }
    };

    private ServiceConnection connection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TaskHandlerInForegroundService.HandlerService.HandlerServiceBinder b = (TaskHandlerInForegroundService.HandlerService.HandlerServiceBinder) iBinder;
            service2 = b.getService();
            cbHandlerForegroundservice.setChecked(service2.isRunning());
            getActivity().unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service2 = null;
        }
    };

/* TODO: Does not work with services in different processes
    private ServiceConnection connection3 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TaskHandlerInForegroundService2.HandlerService.HandlerServiceBinder b = (TaskHandlerInForegroundService2.HandlerService.HandlerServiceBinder) iBinder;
            service3 = b.getService();
            cbHandlerForegroundserviceOwnProcess.setChecked(service3.isRunning());
            getActivity().unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service3 = null;
        }
    };
*/

    public PeriodicTasksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_periodic_tasks, container, false);

        ((EditText)v.findViewById(R.id.etIntervalAlarmSetRepeating)).setText("60");
        ((EditText)v.findViewById(R.id.etIntervalAlarmSetAndAllowWhileIdle)).setText("60");
        ((EditText)v.findViewById(R.id.etIntervalHandlerBackgroundservice)).setText("10");
        ((EditText)v.findViewById(R.id.etIntervalHandlerForegroundservice)).setText("10");
        ((EditText)v.findViewById(R.id.etIntervalHandlerForegroundserviceOwnProcess)).setText("10");
        ((EditText)v.findViewById(R.id.etIntervalHandlerMainthread)).setText("10");

        cbAlarmSetrepeating = (CheckBox)v.findViewById(R.id.cbAlarmSetrepeating);
        cbAlarmSetrepeating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isServicing) {
                    TaskAlarmReceiver task = new TaskAlarmReceiver();

                    if (isChecked)
                        task.startTask(getContext(), true, Long.parseLong(((EditText) v.findViewById(R.id.etIntervalAlarmSetRepeating)).getText().toString()) * 1000L);
                    else
                        task.startTask(getContext(), false, -1);
                }
            }
        });

        cbAlarmSetAndAllowWhileIdle = (CheckBox)v.findViewById(R.id.cbAlarmSetAndAllowWhileIdle);
        cbAlarmSetAndAllowWhileIdle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isServicing) {
                    TaskAlarmReceiverAllowWhileIdle task = new TaskAlarmReceiverAllowWhileIdle();

                    if (isChecked)
                        task.startTask(getContext(), true, Long.parseLong(((EditText) v.findViewById(R.id.etIntervalAlarmSetAndAllowWhileIdle)).getText().toString()) * 1000L);
                    else
                        task.startTask(getContext(), false, -1);
                }
            }
        });

        cbHandlerBackgroundservice = (CheckBox)v.findViewById(R.id.cbHandlerBackgroundservice);
        cbHandlerBackgroundservice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isServicing) {
                    TaskHandlerInBackgroundService task = new TaskHandlerInBackgroundService();

                    if (isChecked)
                        task.startTask(getContext(), true, Long.parseLong(((EditText) v.findViewById(R.id.etIntervalHandlerBackgroundservice)).getText().toString()) * 1000L);
                    else
                        task.startTask(getContext(), false, -1);
                }
            }
        });


        cbHandlerForegroundservice = (CheckBox)v.findViewById(R.id.cbHandlerForegroundservice);
        cbHandlerForegroundservice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isServicing) {
                    TaskHandlerInForegroundService task = new TaskHandlerInForegroundService();

                    if (isChecked)
                        task.startTask(getContext(), true, Long.parseLong(((EditText) v.findViewById(R.id.etIntervalHandlerForegroundservice)).getText().toString()) * 1000L);
                    else
                        task.startTask(getContext(), false, -1);
                }
            }
        });

        cbHandlerForegroundserviceOwnProcess = (CheckBox)v.findViewById(R.id.cbHandlerForegroundserviceOwnProcess);
        cbHandlerForegroundserviceOwnProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isServicing) {
                    TaskHandlerInForegroundService2 task = new TaskHandlerInForegroundService2();

                    if (isChecked)
                        task.startTask(getContext(), true, Long.parseLong(((EditText) v.findViewById(R.id.etIntervalHandlerForegroundserviceOwnProcess)).getText().toString()) * 1000L);
                    else
                        task.startTask(getContext(), false, -1);
                }
            }
        });

        cbHandlerMainthread = (CheckBox)v.findViewById(R.id.cbHandlerMainthread);
        cbHandlerMainthread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isServicing) {
                    TaskHandlerOnMainThread task = new TaskHandlerOnMainThread();

                    if (isChecked)
                        task.startTask(getContext(), true, Long.parseLong(((EditText) v.findViewById(R.id.etIntervalHandlerMainthread)).getText().toString()) * 1000L);
                    else
                        task.startTask(getContext(), false, -1);
                }
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        isServicing = false;

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent(getContext(), TaskHandlerInBackgroundService.HandlerService.class);
        getActivity().bindService(intent, connection1, Context.BIND_AUTO_CREATE);

        intent = new Intent(getContext(), TaskHandlerInForegroundService.HandlerService.class);
        getActivity().bindService(intent, connection2, Context.BIND_AUTO_CREATE);

/* TODO: Does not work with services in different processes
        intent = new Intent(getContext(), TaskHandlerInForegroundService2.HandlerService.class);
        getActivity().bindService(intent, connection3, Context.BIND_AUTO_CREATE);
*/
        cbAlarmSetrepeating.setChecked(TaskAlarmReceiver.isRunning(getContext()));
        cbAlarmSetAndAllowWhileIdle.setChecked(TaskAlarmReceiverAllowWhileIdle.isRunning(getContext()));
        cbHandlerMainthread.setChecked(TaskHandlerOnMainThread.isRunning());

        isServicing = true;
    }
}

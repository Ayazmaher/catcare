package com.app.catcare.ayaz.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.catcare.ayaz.fragments.CreateTaskBottomSheetFragment;
import com.app.catcare.ayaz.adapters.TaskAdapter;
import com.app.catcare.ayaz.broadcastReceiver.AlarmBroadcastReceiver;
import com.app.catcare.ayaz.database.DatabaseClient;
import com.app.catcare.ayaz.databinding.ActivityAddTaskBinding;
import com.app.catcare.ayaz.models.Task;

import java.util.ArrayList;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity implements CreateTaskBottomSheetFragment.setRefreshListener {
    private ActivityAddTaskBinding binding;
    TaskAdapter taskAdapter;
    List<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpAdapter();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ComponentName receiver = new ComponentName(this, AlarmBroadcastReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        callForNotificationPermission();
        binding.tvAddTasks.setOnClickListener(view -> {
            CreateTaskBottomSheetFragment createTaskBottomSheetFragment = new CreateTaskBottomSheetFragment();
            createTaskBottomSheetFragment.setTaskId(0, false, this, AddTaskActivity.this);
            createTaskBottomSheetFragment.show(getSupportFragmentManager(), createTaskBottomSheetFragment.getTag());

        });
        binding.ibBackTask.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        getSavedTasks();
    }

    private void callForNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
                else {
                    // repeat the permission or open app details
                    Toast.makeText(this,"Notification Permission is required for this device.",Toast.LENGTH_SHORT).show();
                }
            }
    }

    public void setUpAdapter() {
        taskAdapter = new TaskAdapter(this, tasks, this);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.rvTasks.setAdapter(taskAdapter);
    }

    private void getSavedTasks() {

        class GetSavedTasks extends AsyncTask<Void, Void, List<Task>> {
            @Override
            protected List<Task> doInBackground(Void... voids) {
                tasks = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .dataBaseAction()
                        .getAllTasksList();
                return tasks;
            }

            @Override
            protected void onPostExecute(List<Task> tasks) {
                super.onPostExecute(tasks);
                binding.ivThumbTask.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
                setUpAdapter();
            }
        }

        GetSavedTasks savedTasks = new GetSavedTasks();
        savedTasks.execute();
    }

    @Override
    public void refresh() {
        getSavedTasks();
    }
}
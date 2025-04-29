package com.example.mobile_project;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import com.example.mobile_project.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<HabitTemplate> habitList = new ArrayList<>();

    private HabitAdapter habitAdapter;
    private RecyclerView recyclerView;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HabitPrefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SecondActivity.class);
                startActivityForResult(i, 1);
            }
        });
        sharedPreferences = getSharedPreferences("HabitPrefs", MODE_PRIVATE);
        loadHabits();
        habitAdapter = new HabitAdapter(habitList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(habitAdapter);

        Snackbar.make(binding.getRoot(), "Tip: Tap to edit, Long press to delete.", Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fab) // So it doesn't overlap FAB
                .show();

        ViewCompat.setTooltipText(binding.fab, "Add new habit");

        for (HabitTemplate habit : habitList) {
            if (habit.isCompleted()) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - habit.getLastCheckedTime();

                if (elapsedTime > 24 * 60 * 60 * 1000) {
                    habit.setCompleted(false);
                    habit.resetStreak();
                }
            }
        }
        habitAdapter.notifyDataSetChanged();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Switch notificationSwitch = findViewById(R.id.notificationSwitch);

        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        notificationSwitch.setChecked(notificationsEnabled);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
                startDailyReminder();
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
                cancelDailyReminder();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String habitTitle = data.getStringExtra("HABIT_TITLE");

            if (habitTitle != null) {
                HabitTemplate newHabit = new HabitTemplate(habitTitle);
                habitList.add(newHabit);
                habitAdapter.notifyItemInserted(habitList.size() - 1);
                saveHabits();
            }
        }
    }

    private void saveHabits() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("habit_count", habitList.size()); // Save how many habits

        for (int i = 0; i < habitList.size(); i++) {
            HabitTemplate habit = habitList.get(i);
            editor.putString("habit_" + i + "_title", habit.getTitle());
            editor.putBoolean("habit_" + i + "_completed", habit.isCompleted());
            editor.putInt("habit_" + i + "_streak", habit.getStreak());
        }

        editor.apply();
    }

    private void loadHabits() {
        int count = sharedPreferences.getInt("habit_count", 0);

        habitList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String title = sharedPreferences.getString("habit_" + i + "_title", null);
            boolean completed = sharedPreferences.getBoolean("habit_" + i + "_completed", false);
            int streak = sharedPreferences.getInt("habit_" + i + "_streak", 0);

            if (title != null) {
                HabitTemplate habit = new HabitTemplate(title);
                habit.setCompleted(completed);
                habit.setStreak(streak);
                habitList.add(habit);
            }
        }
    }

    private void startDailyReminder() {
        Log.d("ALARM", "Inside startDailyReminder()");

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            long triggerTime = System.currentTimeMillis() + 5 * 1000; // 5 seconds for testing

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    } else {
                        Log.e("ALARM", "Cannot schedule exact alarms. Need user permission!");
                        // Optionally guide user to settings if you want
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
                Log.d("ALARM", "Alarm scheduled successfully!");
            } catch (SecurityException e) {
                Log.e("ALARM", "SecurityException: " + e.getMessage());
            }
        }
    }





    private void cancelDailyReminder() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

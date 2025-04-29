package com.example.mobile_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;

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

    @SuppressLint("NotifyDataSetChanged")
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
                .setAnchorView(binding.fab)
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

        if (resultCode == RESULT_OK) {
            String habitTitle = data.getStringExtra("HABIT_TITLE");

            if (requestCode == 1 && habitTitle != null) {
                // Add new habit
                HabitTemplate newHabit = new HabitTemplate(habitTitle);
                habitList.add(newHabit);
                habitAdapter.notifyItemInserted(habitList.size() - 1);
            } else if (requestCode == 2) {
                // Edit existing habit title only
                int editPos = data.getIntExtra("EDIT_POSITION", -1);
                if (editPos != -1 && editPos < habitList.size()) {
                    habitList.get(editPos).setTitle(habitTitle);
                    habitAdapter.notifyItemChanged(editPos);
                }
            }

            saveHabits();
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
}

package com.example.mobile_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.View;
import com.example.mobile_project.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static ArrayList<HabitTemplate> habitList = new ArrayList<>();

    private HabitAdapter habitAdapter;
    private RecyclerView recyclerView;

    private static SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HabitPrefs";

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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadHabits();
        habitAdapter = new HabitAdapter(MainActivity.this, habitList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(habitAdapter);

        Snackbar.make(binding.getRoot(), "Tip: Tap to edit, Long press to delete.", Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fab)
                .show();

        ViewCompat.setTooltipText(binding.fab, "Add new habit");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshHabits();
                saveHabits();
                habitAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Habits refreshed.", Toast.LENGTH_SHORT).show();
            }
        });
        refreshHabits();
        habitAdapter.notifyDataSetChanged();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void refreshHabits() {
        long currentTime = System.currentTimeMillis();

        for (HabitTemplate habit : habitList) {
            long lastChecked = habit.getLastCheckedTime();

            if (habit.isCompleted() && currentTime - lastChecked >= 10 * 1000L) {
                habit.setCompleted(false);
            }

            if (currentTime - lastChecked >= 20 * 1000L) {
                habit.resetStreak();
            }
        }
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
                HabitTemplate newHabit = new HabitTemplate(habitTitle);
                habitList.add(newHabit);
                habitAdapter.notifyItemInserted(habitList.size() - 1);
            } else if (requestCode == 2) {
                int editPos = data.getIntExtra("EDIT_POSITION", -1);
                if (editPos != -1 && editPos < habitList.size()) {
                    habitList.get(editPos).setTitle(habitTitle);
                    habitAdapter.notifyItemChanged(editPos);
                }
            }
            else if(requestCode==3) saveHabits();

            saveHabits();
        }
    }

    public static  void saveHabits() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("habit_count", habitList.size());

        for (int i = 0; i < habitList.size(); i++) {
            HabitTemplate habit = habitList.get(i);
            editor.putString("habit_" + i + "_title", habit.getTitle());
            editor.putBoolean("habit_" + i + "_completed", habit.isCompleted());
            editor.putInt("habit_" + i + "_streak", habit.getStreak());
            editor.putLong("habit_" + i + "_lastChecked", habit.getLastCheckedTime());
        }

        editor.apply();
    }

    private void loadHabits() {
        habitList.clear();
        int count = sharedPreferences.getInt("habit_count", 0);
        for (int i = 0; i < count; i++) {
            String title = sharedPreferences.getString("habit_" + i + "_title", null);
            boolean completed = sharedPreferences.getBoolean("habit_" + i + "_completed", false);
            int streak = sharedPreferences.getInt("habit_" + i + "_streak", 0);
            long lastChecked = sharedPreferences.getLong("habit_" + i + "_lastChecked", 0);

            if (title != null) {
                HabitTemplate habit = new HabitTemplate(title);
                habit.setCompleted(completed);
                habit.setStreak(streak);
                habit.setLastCheckedTime(lastChecked);
                habitList.add(habit);
            }
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        refreshHabits();
        saveHabits();
    }
    @Override
    protected void onResume(){
        super.onResume();
        refreshHabits();
        habitAdapter.notifyDataSetChanged();
        saveHabits();
   }
    @Override
    public void onDestroy(){
        super.onDestroy();
        refreshHabits();
        habitAdapter.notifyDataSetChanged();
        saveHabits();
    }
}


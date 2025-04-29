package com.example.mobile_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {

    private EditText habitEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        habitEditText = findViewById(R.id.habitEditText);
        saveButton = findViewById(R.id.saveHabitButton);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String habitTitle = habitEditText.getText().toString();
                if (!habitTitle.isEmpty()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("HABIT_TITLE", habitTitle);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Close AddHabitActivity
                }
            }
        });
    }

}
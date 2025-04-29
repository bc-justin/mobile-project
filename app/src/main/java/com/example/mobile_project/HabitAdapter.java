package com.example.mobile_project;

import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import javax.sql.DataSource;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder>{

    private ArrayList<HabitTemplate> habitList;
    public HabitAdapter(ArrayList<HabitTemplate> habitList){
        this.habitList = habitList;
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_view, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position) {
        HabitTemplate habit = habitList.get(position);
        holder.titleTextView.setText(habit.getTitle());
        holder.streakTextView.setText("Streak: " + habit.getStreak() + " 🔥");
        holder.completedCheckBox.setChecked(habit.isCompleted());

        // When user clicks the checkbox
        holder.completedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                habit.setCompleted(holder.completedCheckBox.isChecked());

                if (habit.isCompleted()) {
                    habit.incrementStreak();
                } else {
                    habit.resetStreak();
                }

                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                habitList.remove(position);
                notifyItemRemoved(position);
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                HabitTemplate habit = habitList.get(position);

                Intent intent = new Intent(view.getContext(), SecondActivity.class);
                intent.putExtra("HABIT_TITLE", habit.getTitle());
                intent.putExtra("EDIT_POSITION", position); // Pass the position
                ((MainActivity)view.getContext()).startActivityForResult(intent, 2); // 2 = edit request code
            }
        });

        holder.completedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                habit.setCompleted(holder.completedCheckBox.isChecked());

                if (habit.isCompleted()) {
                    habit.incrementStreak();
                    habit.setLastCheckedTime(System.currentTimeMillis());
                } else {
                    habit.resetStreak();
                    habit.setLastCheckedTime(System.currentTimeMillis());
                }

                notifyItemChanged(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView streakTextView;
        CheckBox completedCheckBox;

        public HabitViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.habitTitle);
            streakTextView = itemView.findViewById(R.id.habitStreak);
            completedCheckBox = itemView.findViewById(R.id.habitCheckbox);
        }
    }

}

package com.example.mobile_project;

import static com.example.mobile_project.MainActivity.saveHabits;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private Context context;
    private ArrayList<HabitTemplate> habitList;

    public HabitAdapter(Context context, ArrayList<HabitTemplate> habitList) {
        this.context = context;
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

        // Long press to delete
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder mb = new AlertDialog.Builder(context);
                mb.setIcon(R.drawable.
                        ic_launcher);
                mb.setTitle("Habit Delete");
                mb.setMessage("Are you sure you want to go to delete this habit? "+ habit.getTitle());
                mb.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int pos = holder.getAdapterPosition();
                        HabitTemplate h = habitList.get(pos);
                        habitList.remove(pos);
                        notifyItemRemoved(pos);
                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        intent.putExtra("DELETE_POSITION", pos);
                        ((MainActivity) view.getContext()).startActivityForResult(intent, 3);}} );
                mb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();}});
                Dialog d = mb.create();
                d.show();
                return true;
            }
        });

        // Tap to edit
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mb = new AlertDialog.Builder(context);
                mb.setIcon(R.drawable.
                        ic_launcher);
                mb.setTitle("Habit Editor");
                mb.setMessage("Are you sure you want to edit this habit?" + habit.getTitle());
                mb.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int pos = holder.getAdapterPosition();
                        HabitTemplate h = habitList.get(pos);
                        Intent intent = new Intent(view.getContext(), SecondActivity.class);
                        intent.putExtra("HABIT_TITLE", h.getTitle());
                        intent.putExtra("EDIT_POSITION", pos);
                        ((MainActivity) view.getContext()).startActivityForResult(intent, 2);}} );
                mb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();}});
                Dialog d = mb.create();
                d.show();
                }

            });

        // Checkbox behavior: update streak and time
        holder.completedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = holder.completedCheckBox.isChecked();
                habit.setCompleted(isChecked);
                long now = System.currentTimeMillis();

                if (isChecked) {
                    habit.incrementStreak();
                } else {
                    habit.setStreak(Math.max(0, habit.getStreak() - 1));
                }

                habit.setLastCheckedTime(now);
                notifyItemChanged(holder.getAdapterPosition());
                saveHabits();
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

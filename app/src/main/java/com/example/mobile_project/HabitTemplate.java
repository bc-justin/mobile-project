package com.example.mobile_project;

public class HabitTemplate {

    private String title;
    private boolean isCompleted;
    private int streak;

    private long lastCheckedTime;


    public HabitTemplate(String title) {
        this.title = title;
        this.isCompleted = false;
        this.streak = 0;
        this.lastCheckedTime = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getStreak() {
        return streak;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void incrementStreak() {
        streak++;
    }

    public void resetStreak() {
        streak = 0;
    }

    public long getLastCheckedTime() {
        return lastCheckedTime;
    }

    public void setLastCheckedTime(long lastCheckedTime) {
        this.lastCheckedTime = lastCheckedTime;
    }
}

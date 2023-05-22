package com.example.easyaccess.reminders;

public class ReminderModel {
    private long id;
    private String category,date,time,description;
    private Frequency frequency;

    public ReminderModel(long id, String category, String date, String time, String description, Frequency frequency) {
        this.id = id;
        this.category = category;
        this.date = date;
        this.time = time;
        this.description = description;
        this.frequency = frequency;
    }

    public ReminderModel(){}

    public ReminderModel(String category, String date, String time, String description, Frequency frequency) {
        this.category = category;
        this.date = date;
        this.time = time;
        this.description = description;
        this.frequency = frequency;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }
}

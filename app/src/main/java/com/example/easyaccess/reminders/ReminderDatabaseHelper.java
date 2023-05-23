package com.example.easyaccess.reminders;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REMINDERS = "reminders";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_FREQUENCY = "frequency";

    public ReminderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_REMINDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + COLUMN_CATEGORY + " TEXT NOT NULL, "
                + COLUMN_DATE + " TEXT , "
                + COLUMN_TIME + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + COLUMN_FREQUENCY + " TEXT NOT NULL)";

        sqLiteDatabase.execSQL(createTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long addReminder(ReminderModel reminder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, reminder.getCategory());
        values.put(COLUMN_DATE, reminder.getDate());
        values.put(COLUMN_TIME, reminder.getTime());
        values.put(COLUMN_DESCRIPTION, reminder.getDescription());
        values.put(COLUMN_FREQUENCY, reminder.getFrequency().toString());

        long id = db.insert(TABLE_REMINDERS, null, values);

        db.close();

        return id;
    }

    @SuppressLint("Range")
    public List<ReminderModel> getAllReminders() {
        List<ReminderModel> reminderList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ReminderModel reminder = new ReminderModel();
                reminder.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                reminder.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                reminder.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                reminder.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                reminder.setFrequency(Frequency.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY)).toUpperCase(Locale.ROOT)));

                reminderList.add(reminder);
                Log.d("REMINDER", "ID:" + reminder.getId() + " DATE:" + reminder.getDate() + " TIME:" + reminder.getTime() + " FREQUENCY:" + reminder.getFrequency().toString());
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return reminderList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<ReminderModel> getRemindersByDate(String date) {
        List<ReminderModel> reminderList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "(" + COLUMN_DATE + " = ? OR " + COLUMN_DATE + " = ? ) AND " + "(" + COLUMN_FREQUENCY + " = 'ONCE' OR " + COLUMN_FREQUENCY + " = 'EVERY_MONTH')";
        String[] selectionArgs = {date, date.substring(5)};

        Cursor cursor = db.query(TABLE_REMINDERS, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String currentTime = LocalTime.now().toString().substring(0, 5);
                LocalTime current = LocalTime.parse(currentTime);
                LocalTime reminderTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("time")));
                int comparison = current.compareTo(reminderTime);
                if(comparison < 0) {
                    ReminderModel reminder = new ReminderModel();
                    reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    reminder.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    reminder.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    reminder.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                    reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    reminder.setFrequency(Frequency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)).toUpperCase(Locale.ROOT)));
                    Log.d("REMINDER BY DATE", "ID:" + reminder.getId() + " DATE:" + reminder.getDate() + " TIME:" + reminder.getTime() + " FREQUENCY:" + reminder.getFrequency().toString());
                    reminderList.add(reminder);
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return reminderList;
    }

    public List<ReminderModel> getEveryDayReminders() {
        List<ReminderModel> reminderList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_FREQUENCY + " = 'EVERY_DAY'";
        Cursor cursor = db.query(TABLE_REMINDERS, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    String currentTime = LocalTime.now().toString().substring(0, 5);
                    LocalTime current = LocalTime.parse(currentTime);
                    LocalTime reminderTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("time")));
                    int comparison = current.compareTo(reminderTime);
                    if (comparison < 0) {
                        ReminderModel reminder = new ReminderModel();
                        reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                        reminder.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                        reminder.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                        reminder.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                        reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                        reminder.setFrequency(Frequency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)).toUpperCase(Locale.ROOT)));
                        Log.d("REMINDER DAILY", "ID:" + reminder.getId() + " DATE:" + reminder.getDate() + " TIME:" + reminder.getTime() + " FREQUENCY:" + reminder.getFrequency().toString());
                        reminderList.add(reminder);

                    }
                }
            }
        }
        return reminderList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<ReminderModel> upcomingReminders() {
        //retrieve reminders whose date is after the current date
        List<ReminderModel> reminderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DATE + " > ?";
        String[] selectionArgs = {LocalDate.now().toString()};
        Cursor cursor = db.query(TABLE_REMINDERS, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ReminderModel reminder = new ReminderModel();
                reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                reminder.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                reminder.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                reminder.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                reminder.setFrequency(Frequency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)).toUpperCase(Locale.ROOT)));

                reminderList.add(reminder);
                Log.d("UPCOMING REMINDER", "ID:" + reminder.getId() + " DATE:" + reminder.getDate() + " TIME:" + reminder.getTime() + " FREQUENCY:" + reminder.getFrequency().toString());
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return reminderList;
    }

    public List<ReminderModel> getMonthlyAndEveryDayReminders() {
        List<ReminderModel> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "frequency = 'EVERY_MONTH' OR frequency = 'EVERY_DAY'";
        Cursor cursor = db.query(TABLE_REMINDERS, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ReminderModel reminder = new ReminderModel();
                reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                reminder.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                reminder.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                reminder.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                reminder.setFrequency(Frequency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)).toUpperCase(Locale.ROOT)));

                reminders.add(reminder);
            }
            cursor.close();
        }
        db.close();
        return reminders;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void deleteExpired() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get the current date in the format "yyyy-MM-dd"
        String currentDate = LocalDate.now().toString();

        // Construct the selection query to delete expired reminders
        String selection = "(" + COLUMN_DATE + " < ? OR (" + COLUMN_DATE + " = ? AND " + COLUMN_TIME + " < ? )) AND frequency = 'ONCE'";
        String[] selectionArgs = {currentDate, currentDate, LocalTime.now().toString()};

        // Perform the delete operation
        int rowsDeleted = db.delete(TABLE_REMINDERS, selection, selectionArgs);
        Log.d("DELETE EXPIRED", "Deleted " + rowsDeleted + " expired reminders");

        db.close();
    }

    public void deleteAllReminders() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDERS, null, null);
        db.close();
    }
}


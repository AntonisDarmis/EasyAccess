package com.example.easyaccess.reminders;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

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
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return reminderList;
    }

    public List<ReminderModel> getRemindersByDateAndFrequency(String date, Frequency frequency) {
        List<ReminderModel> reminderList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DATE + " = ? AND " + COLUMN_FREQUENCY + " = ?";
        String[] selectionArgs = {date, frequency.toString()};

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
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return reminderList;
    }

    public int countReminders(String date,Frequency frequency) {
        List<ReminderModel> reminderList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "( " + COLUMN_DATE + " = ? OR " + COLUMN_DATE + "= '' " + " )" + " AND " + COLUMN_FREQUENCY + " = ?";
        String[] selectionArgs = {date, frequency.toString()};

        Cursor cursor = db.query(TABLE_REMINDERS, null, selection, selectionArgs, null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        db.close();

        return count;
    }
}

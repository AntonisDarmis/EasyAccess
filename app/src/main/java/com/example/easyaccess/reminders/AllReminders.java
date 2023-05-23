package com.example.easyaccess.reminders;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.widget.CalendarView;
import android.widget.ImageView;

import com.example.easyaccess.R;

import java.util.Calendar;
import java.util.List;


public class AllReminders extends AppCompatActivity {

    private ImageView voiceButton;
    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reminders);
        //call corresponding function based on user's command -> user asked for upcoming or
        //CalendarView calendar = findViewById(R.id.calendarView);
        // Create an instance of the ReminderDatabaseHelper
        ReminderDatabaseHelper databaseHelper = new ReminderDatabaseHelper(this);
        databaseHelper.getAllReminders();
        // Retrieve the list of upcoming reminders
        List<ReminderModel> upcomingReminders = databaseHelper.upcomingReminders();

        // Iterate over the list and highlight the corresponding dates in the CalendarView
        for (ReminderModel reminder : upcomingReminders) {
            // Extract the year, month, and day from the reminder's date
            String[] dateParts = reminder.getDate().split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Month is zero-based in CalendarView
            int day = Integer.parseInt(dateParts[2]);

            // Set the corresponding date as selected in the CalendarView
            Calendar calendarInstance = Calendar.getInstance();
            calendarInstance.set(year, month, day);
            long timeInMillis = calendarInstance.getTimeInMillis();
           // calendar.setDate(timeInMillis, true, true);
        }
    }
}
package com.example.easyaccess.reminders;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.Help;
import com.example.easyaccess.R;
import com.example.easyaccess.calls.Calls;

import java.util.ArrayList;
import java.util.Locale;


public class Reminder extends AppCompatActivity implements View.OnClickListener {

    ReminderDatabaseHelper databaseHelper;

    private ImageView voiceButton;
    private EditText category, date, time, description;

    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    private RadioGroup radioGroup;

    private String command;

    private String callingActivity;

    private long reminderID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        voiceButton = findViewById(R.id.reminder_voice);
        voiceButton.setOnClickListener(this);
        category = findViewById(R.id.reminder_category);
        date = findViewById(R.id.reminder_date);
        date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // if user has given whole date disable the options for Every day and Every month
                if (date.getText().toString().length() >= 6 && date.getText().toString().length() <= 10) {
                    //whole date, user can only select Once
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonOnce).setVisibility(View.VISIBLE);
                    radioGroup.check(R.id.radioButtonOnce);
                } else if (date.getText().toString().length() == 2) {
                    //Month and day given, user can only select Each Month
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonOnce).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
                    radioGroup.check(R.id.radioButtonRepeat);
                } else if (date.getText().toString().length() == 4) {
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.GONE);
                } else if (time.getText().length() != 0 && date.getText().length() == 0) {
                    findViewById(R.id.radioButtonOnce).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.GONE);
                    radioGroup.check(R.id.radioButtonEveryday);
                } else {
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.VISIBLE);
                    findViewById(R.id.radioButtonOnce).setVisibility(View.VISIBLE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
                    radioGroup.check(R.id.radioButtonOnce);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        time = findViewById(R.id.reminder_time);
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (date.getText().toString().isEmpty() && !time.getText().toString().isEmpty()) {
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonOnce).setVisibility(View.GONE);
                    radioGroup.check(R.id.radioButtonEveryday);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        description = findViewById(R.id.reminder_description);
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.check(R.id.radioButtonOnce);
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        // intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-gr");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //If the activity is being called to edit a reminder, pre fill all the fields
        Intent intent = getIntent();
        String activity = intent.getExtras().getString("callingActivity");
        callingActivity = activity;
        if (activity.equals("AllReminders")) {
            ReminderModel reminder = (ReminderModel) intent.getSerializableExtra("reminderModel");
            reminderID = reminder.getId();
            category.setText(reminder.getCategory());
            description.setText(reminder.getDescription());
            date.setText(reminder.getDate());
            time.setText(reminder.getTime());
            findViewById(R.id.radioButtonOnce).setVisibility(View.VISIBLE);
            findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
            findViewById(R.id.radioButtonEveryday).setVisibility(View.VISIBLE);
            if (reminder.getFrequency().toString().equals("ONCE")) {
                radioGroup.check(R.id.radioButtonOnce);
            } else if (reminder.getFrequency().toString().equals("EVERY_MONTH")) {
                radioGroup.check(R.id.radioButtonRepeat);
            } else {
                radioGroup.check(R.id.radioButtonEveryday);
            }
        }
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                speechRecognizer.stopListening();
            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    command = matches.get(0);
                    command = command.toLowerCase(Locale.ROOT);
                    String[] parts = command.split(" ", 2);
                    Log.d("VOICE COMMAND IN ADD", command);
                    Intent intent;
                    switch (parts[0]) {
                        case "delete": {
                            if (parts.length > 1) {
                                switch (parts[1]) {
                                    case "category": {
                                        category.setText("");
                                        break;
                                    }
                                    case "time": {
                                        time.setText("");
                                        break;
                                    }
                                    case "date": {
                                        date.setText("");
                                        findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
                                        findViewById(R.id.radioButtonEveryday).setVisibility(View.VISIBLE);
                                        findViewById(R.id.radioButtonOnce).setVisibility(View.VISIBLE);
                                        break;
                                    }
                                    case "description":{
                                        description.setText("");
                                    }
                                }
                            } else {
                                category.setText("");
                                time.setText("");
                                date.setText("");
                                description.setText("");
                            }
                            break;
                        }
                        case "category": {
                            if (parts.length > 1) {
                                category.setText(capitalize(parts[1]));
                            }
                            break;
                        }
                        case "description": {
                            if (parts.length > 1) {
                                description.setText(parts[1]);
                            }
                            break;
                        }
                        case "year": {
                            if (parts.length > 1) {
                                date.setText(parts[1]);
                            }
                            break;
                        }
                        case "month": {
                            if (parts.length > 1 && isMonth(parts[1])) {
                                if (date.getText().toString().isEmpty()) {
                                    date.setText(format(parts[1]));
                                } else {
                                    date.append("-" + format(parts[1]));
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid month!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "day": {
                            if (parts.length > 1 && isDayOfMonth(parts[1]) && date.getText().toString().isEmpty()) {
                                date.append(format(parts[1]));
                            } else if (parts.length > 1 && isDayOfMonth(parts[1])) {
                                date.append("-" + format(parts[1]));
                            }
                            break;
                        }
                        case "time": {
                            if (parts.length > 1) {
                                time.setText(format(parts[1]));
                            }
                            break;
                        }
                        case "minutes": {
                            if (parts.length > 1 && !time.getText().toString().isEmpty()) {
                                time.append(":" + format(parts[1]));
                            } else {
                                Toast.makeText(getApplicationContext(), "Please provide the hour first!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "store": {
                            if (callingActivity.equals("Main")) {
                                if (category.getText().toString().isEmpty() && time.getText().toString().isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Please input a category and a time at minimum.", Toast.LENGTH_SHORT).show();
                                } else {
                                    saveReminder();
                                    Reminder.super.onBackPressed();
                                }
                            } else {
                                saveReminder();
                                Reminder.super.onBackPressed();
                            }
                            break;
                        }
                        case "back": {
                            finish();
                        }
                        case "help":{
                            intent = new Intent(Reminder.this, Help.class);
                            intent.putExtra("callingActivity","ReminderActivity");
                            startActivity(intent);
                        }
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    private static final String capitalize(String str) {

        if (str == null || str.length() == 0) return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }

    private static String format(String str) {
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;
    }

    private void saveReminder() {
        //perform save operation logic
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
        Frequency frequency;
        if (selectedRadioButton.getText().toString().equals("Every month")) {
            frequency = Frequency.EVERY_MONTH;
        } else if (selectedRadioButton.getText().toString().equals("Once")) {
            frequency = Frequency.ONCE;
        } else {
            frequency = Frequency.EVERY_DAY;
        }
        //if date is empty and the user tries to save a reminder with option "ONCE" or user didnt provide the complete date do not proceed
        if (frequency.equals(Frequency.ONCE) && date.getText().toString().length() != 10) {
            Toast.makeText(getApplicationContext(), "Please provide a complete date!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.getText().toString().length() == 2 && !(isDayOfMonth(date.getText().toString()))) {
            Toast.makeText(getApplicationContext(), "Please provide a correct day of month", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!in24HourFormat(time.getText().toString()) || time.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Incorrect time provided!", Toast.LENGTH_SHORT).show();
            return;
        }


        databaseHelper = new ReminderDatabaseHelper(getApplicationContext());
        ReminderModel reminder;
        if (frequency.equals(Frequency.EVERY_DAY)) {
            reminder = new ReminderModel(category.getText().toString(), "", time.getText().toString(), description.getText().toString(), frequency);
        } else {
            reminder = new ReminderModel(category.getText().toString(), date.getText().toString(), time.getText().toString(), description.getText().toString(), frequency);
        }

        long successful;
        if (callingActivity.equals("Main")) {
            successful = databaseHelper.addReminder(reminder);
        } else {
            successful = databaseHelper.editReminder(reminderID, reminder);
        }
        //check if it was successful
        if (successful > 0) {
            Toast.makeText(getApplicationContext(), "Reminder set successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
        }

        databaseHelper.close();
    }


    private static boolean in24HourFormat(String time) {
        String timeFormat = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
        return time.matches(timeFormat);
    }

    private static boolean isDayOfMonth(String day) {
        String dayFormat = "(0[1-9]|[12]\\d|3[01])";
        return day.matches(dayFormat);
    }

    private static boolean isMonth(String month) {
        String monthFormat = "^(0[1-9]|1[0-2])$";
        return month.matches(monthFormat);
    }

    @Override
    public void onClick(View view) {
//        saveReminder();
//        Reminder.super.onBackPressed();
        speechRecognizer.startListening(intentRecognizer);
    }


}
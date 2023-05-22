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

import com.example.easyaccess.R;

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
                if (date.getText().toString().length() == 10) {
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.GONE);
                } else if (date.getText().toString().length() == 2 && isDayOfMonth(date.getText().toString())) {
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonOnce).setVisibility(View.GONE);
                } else if (date.getText().toString().length() == 2 && isDayOfMonth(date.getText().toString())) {
                    findViewById(R.id.radioButtonOnce).setVisibility(View.GONE);
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.radioButtonEveryday).setVisibility(View.VISIBLE);
                    findViewById(R.id.radioButtonOnce).setVisibility(View.VISIBLE);
                    findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        time = findViewById(R.id.reminder_time);
        description = findViewById(R.id.reminder_description);
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.check(R.id.radioButtonOnce);
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        // intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-gr");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
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
                    //textView.setText(command);
                    Intent intent;
                    switch (parts[0]) {
                        case "clear": {
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
                                    case "date":
                                        date.setText("");
                                        findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
                                        findViewById(R.id.radioButtonEveryday).setVisibility(View.VISIBLE);
                                        break;
                                }
                            } else {
                                category.setText("");
                                time.setText("");
                                date.setText("");
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
                        case "month":
                        case "day": {
                            if (parts.length > 1) {
                                date.append("-" + format(parts[1]));
                            }
                            break;
                        }
                        case "hour":
                        case "minutes": {
                            if (parts.length > 1) {
                                time.setText(format(parts[1]));
                            }
                            break;
                        }
                        case "every": {
                            if (parts.length > 1) {
                                if (parts[1].equals("day")) {
                                    radioGroup.check(R.id.radioButtonEveryday);
                                } else {
                                    radioGroup.check(R.id.radioButtonRepeat);
                                }
                                break;
                            }

                            break;
                        }
                        case "once": {
                            radioGroup.check(R.id.radioButtonOnce);
                            break;
                        }
                        case "save": {
                            saveReminder();
                            finish();
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
        //check if fields are not empty
        if (category.getText().toString().isEmpty() || category.getText().toString().length() == 0
                || time.getText().toString().isEmpty() || time.getText().toString().length() == 0
                || !(time.getText().toString().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]"))) {
            Toast.makeText(getApplicationContext(), "Please provide an input and a correct format for all the fields", Toast.LENGTH_SHORT).show();
        }

        //perform save operation logic
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
        Frequency frequency;
        if (selectedRadioButton.getText().toString().equals("Repeat")) {
            frequency = Frequency.EVERY_MONTH;
        } else if (selectedRadioButton.getText().toString().equals("Once")) {
            frequency = Frequency.ONCE;
        } else {
            frequency = Frequency.EVERY_DAY;
        }
        //if date is empty and the user tries to save a reminder with option "ONCE" or user didnt provide the complete date do not proceed
        if ((!(date.getText().toString().matches("\\d{4}-\\d{2}-\\d{2}")) || date.getText().toString().isEmpty() || date.getText().toString().length() == 0)
                && frequency.equals(Frequency.ONCE)) {
            Toast.makeText(getApplicationContext(), "Please provide a correct date!", Toast.LENGTH_SHORT).show();
        }

        if (date.getText().toString().length() == 2 && !(isDayOfMonth(date.getText().toString()))) {
            Toast.makeText(getApplicationContext(), "Please provide a correct day of month", Toast.LENGTH_SHORT).show();
        }


        databaseHelper = new ReminderDatabaseHelper(getApplicationContext());
        ReminderModel reminder;
        if (frequency.equals(Frequency.EVERY_DAY)) {
            reminder = new ReminderModel(category.getText().toString(), "", time.getText().toString(), description.getText().toString(), frequency);
        } else {
            reminder = new ReminderModel(category.getText().toString(), date.getText().toString(), time.getText().toString(), description.getText().toString(), frequency);
        }
        long successful = databaseHelper.addReminder(reminder);
        if (successful != -1) {
            Toast.makeText(getApplicationContext(), "Reminder set successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
        }
        databaseHelper.close();
    }

    private static boolean isDayOfMonth(String day) {
        String dayFormat = " ^(?:[1-9]|[12][0-9]|3[01]|0[1-9])$ ";
        return day.matches(dayFormat);
    }

    private static boolean isMonth(String month) {
        String monthFormat = "^(?:[1-9]|1[0-2])$";
        return month.matches(monthFormat);
    }

    @Override
    public void onClick(View view) {
        speechRecognizer.stopListening();
    }


}
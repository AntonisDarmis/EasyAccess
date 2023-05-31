package com.example.easyaccess.reminders;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.ExplanationDialogHelper;
import com.example.easyaccess.Help;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.Locale;


public class Reminder extends AppCompatActivity implements View.OnClickListener {

    private PopupWindow popupWindow;
    private TextView messageTextView;

    private ReminderDatabaseHelper databaseHelper;

    private ImageView voiceButton;
    private EditText category, date, time, description;

    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    private RadioGroup radioGroup;

    private String command;

    private String callingActivity;

    private long reminderID;

    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;

    private void showExplanationDialog() {
        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set the language to the appropriate locale
                    textToSpeech.setLanguage(Locale.US);

                    // Create and set the UtteranceProgressListener
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            // TTS started speaking, if needed
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            // TTS finished speaking, dismiss the dialog
                            alertDialog.dismiss();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            // TTS encountered an error, if needed
                        }

                        @Override
                        public void onRangeStart(String utteranceId, int start, int end, int frame) {
                            // Update the dialog text as TTS speaks each word
                            String dialogText = dialogTextView.getText().toString();
                            dialogTextView.setText(dialogText);
                        }
                    });

                    // Create the dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(Reminder.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(Reminder.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality for storing a reminder. Through this activity, by using the correct commands,you can set the category," +
                            "description, date, time and frequency of the reminder or edit an existing one.\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }


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
        radioGroup = findViewById(R.id.radioGroupTransport);
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
                messageTextView.setText("Processing...");
                speechRecognizer.stopListening();
            }

            @Override
            public void onError(int i) {
                popupWindow.dismiss();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    command = matches.get(0);
                    messageTextView.setText(command);
                    command = command.toLowerCase(Locale.ROOT);
                    String[] parts = command.split(" ", 2);
                    Log.d("VOICE COMMAND IN ADD", command);
                    Intent intent;
                    switch (parts[0]) {
                        case "delete": {
                            if (parts.length > 1) {
                                switch (parts[1]) {
                                    case "category": {
                                        popupWindow.dismiss();
                                        category.setText("");
                                        break;
                                    }
                                    case "time": {
                                        popupWindow.dismiss();
                                        time.setText("");
                                        break;
                                    }
                                    case "date": {
                                        popupWindow.dismiss();
                                        date.setText("");
                                        findViewById(R.id.radioButtonRepeat).setVisibility(View.VISIBLE);
                                        findViewById(R.id.radioButtonEveryday).setVisibility(View.VISIBLE);
                                        findViewById(R.id.radioButtonOnce).setVisibility(View.VISIBLE);
                                        break;
                                    }
                                    case "description": {
                                        popupWindow.dismiss();
                                        description.setText("");
                                    }
                                }
                            } else {
                                popupWindow.dismiss();
                                category.setText("");
                                time.setText("");
                                date.setText("");
                                description.setText("");
                            }
                            break;
                        }
                        case "category": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                category.setText(capitalize(parts[1]));
                            }
                            break;
                        }
                        case "description": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                description.setText(parts[1]);
                            }
                            break;
                        }
                        case "year": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                date.setText(parts[1]);
                            }
                            break;
                        }
                        case "month": {
                            popupWindow.dismiss();
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
                            popupWindow.dismiss();
                            if (parts.length > 1 && isDayOfMonth(parts[1]) && date.getText().toString().isEmpty()) {
                                date.append(format(parts[1]));
                            } else if (parts.length > 1 && isDayOfMonth(parts[1])) {
                                date.append("-" + format(parts[1]));
                            }
                            break;
                        }
                        case "time": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                if (parts[1].matches("^([01]?[0-9]|2[0-3])$")) {
                                    time.setText(format(parts[1]));
                                } else {
                                    Toast.makeText(Reminder.this, "Invalid hour format!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        }
                        case "minutes": {
                            popupWindow.dismiss();
                            if (parts.length > 1 && !time.getText().toString().isEmpty() && time.getText().toString().length() == 2) {
                                if (parts[1].matches("^[0-5][0-9]$")) {
                                    String hours = time.getText().toString();
                                    time.setText(hours + ":" + format(parts[1]));
                                } else {
                                    Toast.makeText(Reminder.this, "Invalid minutes format!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Please provide the hour first!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "store": {
                            popupWindow.dismiss();
                            if (callingActivity.equals("Main")) {
                                if (category.getText().toString().isEmpty() && time.getText().toString().isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Please input a category and a time at minimum.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if(saveReminder()) {
                                        Reminder.super.onBackPressed();
                                    }
                                }
                            } else {
                                if(saveReminder()) {
                                    Reminder.super.onBackPressed();
                                }
                            }
                            break;
                        }
                        case "buck":
                        case "back": {
                            popupWindow.dismiss();
                            finish();
                            break;
                        }
                        case "help": {
                            popupWindow.dismiss();
                            intent = new Intent(Reminder.this, Help.class);
                            intent.putExtra("callingActivity", "ReminderActivity");
                            startActivity(intent);
                            break;
                        }
                        case "explain": {
                            popupWindow.dismiss();
                            voiceButton.setEnabled(false);
                            showExplanationDialog();
                            voiceButton.setEnabled(true);
                            break;
                        }
                    }
                    popupWindow.dismiss();
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

    private boolean saveReminder() {
        boolean correctFormats = true;
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
            correctFormats = false;
        }

        if (date.getText().toString().length() == 2 && !(isDayOfMonth(date.getText().toString()))) {
            Toast.makeText(getApplicationContext(), "Please provide a correct day of month", Toast.LENGTH_SHORT).show();
            correctFormats = false;
        }

        if (!in24HourFormat(time.getText().toString()) || time.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Incorrect time provided!", Toast.LENGTH_SHORT).show();
            correctFormats = false;
        }

        if(time.getText().toString().isEmpty() || category.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Time or category are empty!", Toast.LENGTH_SHORT).show();
            correctFormats = false;
        }

        if(correctFormats) {
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
            return true;
        }
        else{
            Toast.makeText(this, "Please check if all inputs are in the correct format", Toast.LENGTH_SHORT).show();
            return false;
        }
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
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // Create the popup window
        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Find the TextView in the popup layout
        messageTextView = popupView.findViewById(R.id.messageTextView);

        // Show the popup window at the center of the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        speechRecognizer.startListening(intentRecognizer);
    }


}
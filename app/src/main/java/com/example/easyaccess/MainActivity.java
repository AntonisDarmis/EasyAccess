package com.example.easyaccess;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_CONTACTS;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.calls.Calls;
import com.example.easyaccess.maps.ChoiceActivity;
import com.example.easyaccess.notes.AddNote;
import com.example.easyaccess.notes.AllNotes;
import com.example.easyaccess.reminders.AllReminders;
import com.example.easyaccess.reminders.Reminder;
import com.example.easyaccess.reminders.ReminderAdapter;
import com.example.easyaccess.reminders.ReminderDatabaseHelper;
import com.example.easyaccess.reminders.ReminderModel;
import com.example.easyaccess.sms.SMS;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;
    String[] permissions = {
            RECORD_AUDIO,
            INTERNET,
            READ_CONTACTS,
            WRITE_CONTACTS,
            CALL_PHONE,
            READ_SMS,
            SEND_SMS,
            RECEIVE_SMS,
            READ_CALL_LOG,
            ACCESS_COARSE_LOCATION};


    private SpeechRecognizer speechRecognizer;
    private Button button1, button2, button3;
    private Intent intentRecognizer;
    private String command;
    private TextView textView;

    private ImageView voiceButton;
    private ReminderDatabaseHelper databaseHelper;


    private ReminderAdapter adapter;
    private RecyclerView recyclerView;


    private List<ReminderModel> reminders = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check if the permissions are already granted
        if (arePermissionsGranted()) {
            // Permissions are already granted
            // Proceed with your logic here
            // ...
        } else {
            // Request permissions at runtime
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        adapter = new ReminderAdapter(this, reminders);
        recyclerView = findViewById(R.id.reminderRecycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        voiceButton = findViewById(R.id.main_voice);
        voiceButton.setOnClickListener(this);

        button1 = findViewById(R.id.button);
        button1.setOnClickListener(this);

        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);

        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);

        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-gr");
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
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
                    // textView.setText(command);
                    command = matches.get(0);
                    command = command.toLowerCase(Locale.ROOT);
                    String[] parts = command.split(" ", 2);
                    Log.d("VOICE COMMAND IN ADD", command);
                    //textView.setText(command);
                    Intent intent;
                    switch (parts[0]) {
                        case "contacts": {
                            //open calls activity
                            intent = new Intent(MainActivity.this, Calls.class);
                            startActivity(intent);

                            break;
                        }
                        case "maps": {
                            //open maps activity
                            intent = new Intent();
                            break;
                        }
                        case "sms": {
                            //open sms activity
                            intent = new Intent(MainActivity.this, SMS.class);
                            startActivity(intent);
                            break;
                        }
                        case "new": {
                            if (parts.length > 1) {
                                if (parts[1].equals("reminder")) {
                                    intent = new Intent(MainActivity.this, Reminder.class);
                                    intent.putExtra("callingActivity", "Main");
                                    startActivity(intent);
                                } else if (parts[1].equals("note")) {
                                    intent = new Intent(MainActivity.this, AddNote.class);
                                    intent.putExtra("callingActivity", "Main");
                                    startActivity(intent);
                                }
                            }
                            break;

                        }
                        case "reminders": {
                            intent = new Intent(MainActivity.this, AllReminders.class);
                            startActivity(intent);
                            break;
                        }
                        case "notes": {
                            intent = new Intent(MainActivity.this, AllNotes.class);
                            startActivity(intent);
                            break;
                        }
                        case "help": {
                            //prompt help toolbox with text to speech bot??
                            intent = new Intent(MainActivity.this, Help.class);
                            intent.putExtra("callingActivity", "MainActivity");
                            startActivity(intent);
                            break;
                        }
                        case "explain": {
                            voiceButton.setEnabled(false);
                            showExplanationDialog();
                            voiceButton.setEnabled(true);
                            break;
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

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String currentDate = LocalDate.now().toString();
            String currentTime = LocalTime.now().toString().substring(0, 5);
            databaseHelper = new ReminderDatabaseHelper(getApplicationContext());
            databaseHelper.deleteExpired();

            List<ReminderModel> onceAndMonthlyReminders = databaseHelper.getRemindersByDate(currentDate);
            List<ReminderModel> dailyReminders = databaseHelper.getEveryDayReminders();
            databaseHelper.getAllReminders();
            reminders.clear();
            reminders.addAll(onceAndMonthlyReminders);
            reminders.addAll(dailyReminders);

            adapter.notifyDataSetChanged();
            showReminderNotification(reminders.size());
            //int total = onceReminders + monthlyReminders + dailyReminders;

        }
    }


    private void showReminderNotification(int total) {
        View rootView = findViewById(android.R.id.content);
        String message = "You have " + total + " reminders for today!";
        int duration = Snackbar.LENGTH_LONG;
        Snackbar snackbar = Snackbar.make(rootView, message, duration);
        snackbar.setBackgroundTint(getResources().getColor(R.color.light_blue));
        snackbar.setTextColor(getResources().getColor(R.color.black));
        snackbar.show();
    }


    @Override
    public void onClick(View view) {
        //speechRecognizer.startListening(intentRecognizer);
        switch (view.getId()) {
            case R.id.button: {
                Intent intent = new Intent(this, Reminder.class);
                intent.putExtra("callingActivity", "Main");
                startActivity(intent);
                break;
            }
            case R.id.button3: {
                Intent intent = new Intent(this, AllReminders.class);
                startActivity(intent);
                break;
            }
            case R.id.button2: {
                Intent intent = new Intent(this, AddNote.class);
                intent.putExtra("callingActivity", "Main");
                startActivity(intent);
            }
            case R.id.main_voice: {
                speechRecognizer.startListening(intentRecognizer);
            }
        }
    }

    private boolean arePermissionsGranted() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                Toast.makeText(getApplicationContext(), "Some permissions are not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }


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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity is the base of the application. Through this activity, by using the correct commands, you can navigate through " +
                            "the whole application and its functionalities.\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }


    public void openApplication(String app) {
        return;
    }
}
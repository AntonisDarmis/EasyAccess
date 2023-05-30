package com.example.easyaccess;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
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
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.calls.Calls;
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


    private static final int PERMISSION_REQUEST_CODE = 1;
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
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION};
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;

    private ImageView voiceButton;
    private ReminderDatabaseHelper databaseHelper;
    private ReminderAdapter adapter;
    private RecyclerView recyclerView;

    private PopupWindow popupWindow;
    private TextView messageTextView;

    private List<ReminderModel> reminders = new ArrayList<>();

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

        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
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
                messageTextView.setText("Processing...");
                speechRecognizer.stopListening();

            }

            @Override
            public void onError(int i) {
                messageTextView.setText("Error occurred");
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    // textView.setText(command);
                    command = matches.get(0);
                    messageTextView.setText(command);
                    command = command.toLowerCase(Locale.ROOT);
                    String[] parts = command.split(" ", 2);

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
                            ExplanationDialogHelper explanationDialogHelper = new ExplanationDialogHelper(getApplicationContext());
                            String dialogMessage = "This activity is the base of the application. Through this activity, by using the correct commands, you can navigate through " +
                                    "the whole application and its functionalities.\nSay 'HELP' to view the available commands!";
                            explanationDialogHelper.showExplanationDialog(dialogMessage);
                            explanationDialogHelper.shutdown();
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
                finish();
            }
        }
    }


    public void openApplication(String app) {
        return;
    }
}
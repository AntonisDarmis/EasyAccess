package com.example.easyaccess.reminders;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class AllReminders extends AppCompatActivity implements View.OnClickListener {

    private ImageView voiceButton;
    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    private List<ReminderModel> reminders = new ArrayList<>();

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;

    private ReminderDatabaseHelper databaseHelper;

    private String command;
    private int recyclerPosition = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reminders);
        ReminderDatabaseHelper databaseHelper = new ReminderDatabaseHelper(this);
        databaseHelper.getAllReminders();
        voiceButton = findViewById(R.id.all_voice);
        voiceButton.setOnClickListener(this);
        adapter = new ReminderAdapter(this, reminders);
        recyclerView = findViewById(R.id.allRemindersRecycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                        case "back": {
                            finish();
                            break;
                        }
                        case "scroll": {
                            if (parts.length > 1) {
                                if (parts[1].equals("down")) {
                                    recyclerPosition += 3;
                                } else {
                                    recyclerPosition -= 3;
                                    if (recyclerPosition < 0) recyclerPosition = 0;
                                }
                                recyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.smoothScrollToPosition(recyclerPosition);
                                    }
                                }, 500);
                                break;
                            }
                        }
                        case "edit": {
                            if (parts.length > 1) {
                                /* handle logic based on Frequency type
                                /for EVERY_DAY -> user can edit Category,Description, time
                                /for EVERY_MONTH -> user can edit Category, Description, month-day, time
                                 */
                                //user input will be EDIT "NUM_ID" -> example EDIT 5
                                //check if exists
                                Optional<ReminderModel> reminder = reminders.stream().findFirst().filter(reminderModel -> reminderModel.getId() == Integer.parseInt(parts[1]));
                                if (reminder.isPresent()) {
                                    intent = new Intent(AllReminders.this, Reminder.class);
                                    intent.putExtra("callingActivity", "AllReminders");
                                    intent.putExtra("reminderModel", reminder.get());
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(getApplicationContext(), "No reminder with given id found...", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            }
                            break;
                        }
                        case "delete": {
                            //handle delete logic -> delete reminder by ID and display custom popup box to ask if user is sure and start recognizer
                            if (parts.length > 1) {
                                Optional<ReminderModel> reminder = reminders.stream().findFirst().filter(reminderModel -> reminderModel.getId() == Integer.parseInt(parts[1]));
                                if (reminder.isPresent()) {
                                    boolean isDeleted = databaseHelper.deleteById(reminder.get().getId());
                                    if (isDeleted) {
                                        Toast.makeText(getApplicationContext(), "Deleted reminder successfully", Toast.LENGTH_SHORT).show();
                                        reminders.remove(reminder.get());
                                        adapter.notifyDataSetChanged();
                                        break;
                                    } else {
                                        break;
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "No reminder with given id found...", Toast.LENGTH_SHORT).show();
                                }
                            }
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
        databaseHelper = new ReminderDatabaseHelper(getApplicationContext());
        reminders.clear();
        reminders.addAll(databaseHelper.getAllReminders());
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View view) {
        Optional<ReminderModel> reminder = reminders.stream().findFirst().filter(reminderModel -> reminderModel.getId() == Integer.parseInt("1"));
        if (reminder.isPresent()) {
            Intent intent = new Intent(AllReminders.this, Reminder.class);
            intent.putExtra("callingActivity", "AllReminders");
            intent.putExtra("reminderModel", reminder.get());
            startActivity(intent);
        }
        //speechRecognizer.startListening(intentRecognizer);
    }
}

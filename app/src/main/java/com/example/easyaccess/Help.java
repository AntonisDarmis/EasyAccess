package com.example.easyaccess;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Help extends AppCompatActivity implements View.OnClickListener {

    HashMap<String, List<String>> activityCommands = new HashMap<>();
    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;
    private String command;

    private ImageView voiceButton;

    private TextToSpeech textToSpeech;
    private boolean isTTSInitialized = false;

    private String activity;

    private int recyclerPosition = 0;
    private PopupWindow popupWindow;
    private TextView messageTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        voiceButton = findViewById(R.id.help_voice);
        voiceButton.setOnClickListener(this);

        activityCommands.put("MainActivity", Arrays.asList("Contacts : Opens the contacts list", "Maps : View choices for maps", "SMS : Opens the SMS list",
                "New reminder : Add a reminder", "New note : Add a note", "Reminders : View Reminders", "Notes : View notes", "Help : View available commands"));

        activityCommands.put("AddContactActivity", Arrays.asList("Name : Set a name for the contact", "Number : Set a number for the contact",
                "Image : Select an image for the contact", "Store : Save the contact", "Back : Go back"));

        activityCommands.put("CallsActivity", Arrays.asList("Create : Create a contact", "Edit [name] : Edit a contact's details",
                "View [input] : Filters the list based on the speaker's input", "Clear: Clears the filter", "Delete [name] : Delete the contact with given name",
                "Back :Go Back", "Scroll down/up : Scrolls the list", "Call [Contact Name or Number] : Makes a call",
                "Recent [Optional name] : Call history (With provided contact)"));

        activityCommands.put("HistoryActivity", Arrays.asList("Back : Go back", "Scroll down/up : Scrolls the list"));


        activityCommands.put("NoteActivity", Arrays.asList("Back : Go back", "Clear [title/note] : Clears the note's title or the note", "Title [input] : Sets the title",
                "Note [input] : Sets the note's context", "New line : Appends a new line to the note's context", "Store : Saves the note"));

        activityCommands.put("AllNotesActivity", Arrays.asList("Back : Go back", "Open [note number] : Provide a note's number and view/edit it",
                "Delete [note number] : Delete corresponding note", "Scroll down/up : Scrolls the list"));

        activityCommands.put("AllRemindersActivity", Arrays.asList("Back : Go back", "Scroll down/up : Scrolls the list", "Edit [reminder ID] : Edit reminder"
                , "Delete [reminder ID] : Delete reminder"));

        activityCommands.put("ReminderActivity", Arrays.asList("Back : Go back", "Delete [category/description/date/time] : Deletes either the category, description, date, or time",
                "Category [input] : Sets the category", "Description [input] : Sets the description", "Year [input year] : Sets the year", "Month [input month] : Sets the month",
                "Day [input day] : Sets the day", "Time [input time in 24h format] : Sets the time", "Minutes [input minutes] : Sets the minutes", "Store : Saves the reminder",
                "In order for the reminder to be stored all inputs should cohere to the specified rules and formats!"));

        activityCommands.put("SMSActivity", Arrays.asList("Conversation [contactName] : Opens the conversation with the contact", "Scroll down/up : Scrolls the list",
                "View [input] : filters the SMS list", "Clear : Clears the filter", "New [input number] : Opens a new conversation with the number", "Back : Go back"));

        activityCommands.put("NewSMSActivity", Arrays.asList("Message [input] : Sets the message context", "Clear : Clears the message", "Send : Sends the message", "Back : Go back"));

        activityCommands.put("ChatActivity", Arrays.asList("Scroll down/up : Scrolls the conversation", "Back : Go back",
                "Search [input] : Searches the conversation for the specified message", "Next : If matches were found, proceeds to the next",
                "Previous : If matches were found, proceeds to the previous match", "Clear : Clears the search",
                "Message [input] : Sets the message's context", "Send : Sends the message"));

        activityCommands.put("ChoiceActivity", Arrays.asList("Directions : Opens the directions activity", "Categories : Opens the categories activity",
                "Location : Opens maps with current location"));

        activityCommands.put("CategoriesActivity", Arrays.asList("Scroll down/up : Scrolls the category list accordingly", "Back : Go back", "Destination [address or area] : " +
                "Provide the area you want view the categories for, or leave empty and view based on your current location", "[SubCategory] : By saying the title" +
                "of any subcategory from the Categories list, it is marked as checked or unchecked(if it is checked already","Open : Opens the map"));

        activityCommands.put("DirectionsActivity", Arrays.asList("Destination [address or area] : Sets the destination for which directions will be displayed",
                "Start [address/area or empty] : ", "Transport [car/public transport/on foot] : Sets the means of transport",
                "Avoid tolls: If car option is selected, avoid tolls for route",
                "Avoid motorways: If car option is selected, avoid highways for route.", "Avoid ferries: If car option is selected, avoid ferries.",
                "(Not a command!) In the case of public transport as selected option, by default it finds a wheelchair accessible route.",
                "Directions: Opens maps and displays the directions for the selected route."));

        Intent intent = getIntent();
        String callingActivity = intent.getStringExtra("callingActivity");
        activity = callingActivity;

        List<String> commands = activityCommands.get(callingActivity);
        ListView commandListView = findViewById(R.id.commands);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commands);
        commandListView.setAdapter(adapter);


        setTTS();

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
                    //textView.setText(command);
                    Intent intent;
                    switch (parts[0]) {
                        case "buck":
                        case "back": {
                            popupWindow.dismiss();
                            finish();
                            break;
                        }
                        case "read": {
                            popupWindow.dismiss();
                            isTTSInitialized = true;
                            readAllCommands();
                            break;
                        }
                        case "scroll": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                if (parts[1].equals("down")) {
                                    recyclerPosition += 3;
                                } else {
                                    recyclerPosition -= 3;
                                    if (recyclerPosition < 0) recyclerPosition = 0;
                                }
                                commandListView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        commandListView.smoothScrollToPosition(recyclerPosition);
                                    }
                                }, 500);
                                break;
                            }
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

    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void readAllCommands() {
        List<String> commands = activityCommands.get(activity);
        if (isTTSInitialized) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < commands.size(); i++) {
                        if(!isTTSInitialized){
                            break;
                        }
                        String helpCommand = commands.get(i);
                        String utteranceId = String.valueOf(System.currentTimeMillis());
                        textToSpeech.speak(helpCommand, TextToSpeech.QUEUE_ADD, null, utteranceId);

                        // Wait until the command is finished being spoken or the user says "stop"
                        while (textToSpeech.isSpeaking()) {
                            try {
                                Thread.sleep(300); // Adjust the delay time as needed
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    isTTSInitialized = false;
                }
            }).start();
        }
    }

    private void setTTS() {
        textToSpeech = new TextToSpeech(Help.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    isTTSInitialized = false;
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                textToSpeech.stop();
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (isTTSInitialized) {
            isTTSInitialized = false;
            textToSpeech.stop();
            textToSpeech.shutdown();
            Toast.makeText(Help.this, "Text to speech stopped!", Toast.LENGTH_SHORT).show();
            setTTS();
        } else {
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
}
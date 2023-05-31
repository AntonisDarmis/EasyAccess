package com.example.easyaccess.notes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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

public class AddNote extends AppCompatActivity implements View.OnClickListener {

    private PopupWindow popupWindow;
    private TextView messageTextView;


    private ImageView voiceButton;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;

    private EditText title;

    private EditText description;

    private NoteDatabaseHelper noteDatabaseHelper;

    private String callingActivity;

    private int noteID;

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(AddNote.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality for creating and editing notes.\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }

    private static String capitalize(String str) {

        if (str == null || str.length() == 0) return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        voiceButton = findViewById(R.id.addNote_voice);
        voiceButton.setOnClickListener(this);

        title = findViewById(R.id.notes_title);
        description = findViewById(R.id.notes_note);

        Intent intent = getIntent();
        String activity = intent.getExtras().getString("callingActivity");
        callingActivity = activity;
        if (activity.equals("AllNotes")) {
            Note note = (Note) intent.getSerializableExtra("note");
            noteID = note.getId();
            title.setText(note.getTitle());
            description.setText(note.getDescription());
        }

        //speechRecognizer.startListening(intentRecognizer);
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
                        case "buck":
                        case "back": {
                            popupWindow.dismiss();
                            finish();
                            break;
                        }
                        case "clear": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                if (parts[1].equals("title")) {
                                    title.setText("");
                                } else if (parts[1].equals("note")) {
                                    description.setText("");
                                    findViewById(R.id.textView12).setVisibility(View.GONE);
                                }
                            }
                            break;
                        }
                        case "title": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                title.setText(capitalize(parts[1]));
                            }
                            break;
                        }
                        case "note": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                findViewById(R.id.textView12).setVisibility(View.GONE);
                                if (description.getText().toString().isEmpty()) {
                                    description.setText(parts[1]);
                                } else {
                                    description.append(parts[1]);
                                }
                            }
                            break;
                        }
                        case "new": {
                            popupWindow.dismiss();
                            if (parts.length > 1 && parts[1].equals("line")) {
                                description.append("\n");
                            }
                            break;
                        }
                        case "store": {
                            popupWindow.dismiss();
                            if ((title.getText().toString().isEmpty() && description.getText().toString().isEmpty()) || title.getText().toString().length() > 12) {
                                Toast.makeText(getApplicationContext(), "Please provide a correct title and a description ", Toast.LENGTH_SHORT).show();
                            } else {
                                noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
                                long successful = 0;
                                Note note = new Note(title.getText().toString(), description.getText().toString());
                                if (callingActivity.equals("Main")) {
                                    Log.d("EDIT NOTE",  note.getTitle() + " " + note.getDescription());
                                    successful = noteDatabaseHelper.addNote(note);
                                } else {
                                    //handle edit note logic
                                    note.setTitle(title.getText().toString());
                                    note.setDescription(description.getText().toString());
                                    Log.d("EDIT NOTE", "ID" + note.getId()+" " + note.getTitle() + " " + note.getDescription());
                                    successful = noteDatabaseHelper.updateNoteById(noteID, note);
                                }
                                if (successful < 0) {
                                    Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddNote.this, "Note saved successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                            break;
                        }
                        case "help": {
                            popupWindow.dismiss();
                            intent = new Intent(AddNote.this, Help.class);
                            intent.putExtra("callingActivity", "AddNoteActivity");
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
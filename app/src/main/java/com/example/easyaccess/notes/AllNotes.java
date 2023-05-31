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
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.ExplanationDialogHelper;
import com.example.easyaccess.Help;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.NumberConverter;
import com.example.easyaccess.R;
import com.example.easyaccess.calls.Calls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AllNotes extends AppCompatActivity implements View.OnClickListener {
    List<Note> notes = new ArrayList<>();
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ImageView voiceButton;
    private NoteDatabaseHelper noteDatabaseHelper;

    private int recyclerPosition = 0;

    private PopupWindow popupWindow;
    private TextView messageTextView;

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AllNotes.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(AllNotes.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality for viewing notes. Through this activity, by using the correct commands,you can edit and view a specific note" +
                            ".\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notes);
        voiceButton = findViewById(R.id.note_voice);
        voiceButton.setOnClickListener(this);

        adapter = new NoteAdapter(this, notes);
        recyclerView = findViewById(R.id.notesRecycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);


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
                messageTextView.setText("Processing");
                speechRecognizer.stopListening();
            }

            @Override
            public void onError(int i) {
                popupWindow.dismiss();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                popupWindow.dismiss();
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
                        case "view": {
                            //handle edit note logic
                            if (parts.length > 1) {
                                popupWindow.dismiss();
                                long id = NumberConverter.convertWordsToNumber(parts[1]);
                                //implement logic, check if note exists in list
                                Optional<Note> note = notes.stream().findFirst().filter(n -> n.getId() == id);
                                if (note.isPresent()) {
                                    intent = new Intent(AllNotes.this, AddNote.class);
                                    intent.putExtra("callingActivity", "AllNotes");
                                    intent.putExtra("note", note.get());
                                    startActivity(intent);
                                    break;
                                } else {
                                    Toast.makeText(getApplicationContext(), "No note with given ID found!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        }
                        case "delete": {
                            if (parts.length > 1) {
                                popupWindow.dismiss();
                                //handle delete logic, if note exists in list
                                long id = NumberConverter.convertWordsToNumber(parts[1]);
                                Optional<Note> note = notes.stream().findFirst().filter(n -> n.getId() == id);
                                if (note.isPresent()) {
                                    noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
                                    if (noteDatabaseHelper.deleteNoteById(note.get().getId())) {
                                        Toast.makeText(getApplicationContext(), "Note deleted successfully", Toast.LENGTH_SHORT).show();
                                        notes.remove(note.get());
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                        noteDatabaseHelper.close();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "No note with given ID found!", Toast.LENGTH_SHORT).show();
                                }
                            }
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
                                recyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.smoothScrollToPosition(recyclerPosition);
                                    }
                                }, 500);
                                break;
                            }
                        }
                        case "help":{
                            popupWindow.dismiss();
                            intent = new Intent(AllNotes.this, Help.class);
                            intent.putExtra("callingActivity","AllNotesActivity");
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





    @Override
    protected void onResume() {
        super.onResume();
        noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
        notes.clear();
        notes.addAll(noteDatabaseHelper.getAllNotes());
        adapter.notifyDataSetChanged();
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
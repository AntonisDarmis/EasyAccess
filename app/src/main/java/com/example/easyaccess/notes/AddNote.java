package com.example.easyaccess.notes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.Locale;

public class AddNote extends AppCompatActivity implements View.OnClickListener {

    private ImageView voiceButton;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;

    private EditText title;

    private EditText description;

    private NoteDatabaseHelper noteDatabaseHelper;

    private String callingActivity;

    private long noteID;

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
                        case "back": {
                            finish();
                            break;
                        }
                        case "clear": {
                            if (parts.length > 1) {
                                if (parts[1].equals("title")) {
                                    title.setText("");
                                } else if (parts[1].equals("note")) {
                                    description.setText("");
                                }
                            }
                            break;
                        }
                        case "title": {
                            if (parts.length > 1) {
                                title.setText(capitalize(parts[1]));
                            }
                            break;
                        }
                        case "note": {
                            if (parts.length > 1) {
                                description.setText(parts[1]);
                            }
                            break;
                        }
                        case "new": {
                            if (parts.length > 1 && parts[1].equals("line")) {
                                description.append("\n");
                            }
                            break;
                        }
                        case "store": {
                            if ((title.getText().toString().isEmpty() && description.getText().toString().isEmpty()) || title.getText().toString().length() > 12) {
                                Toast.makeText(getApplicationContext(), "Please provide a correct title and a description ", Toast.LENGTH_SHORT).show();
                            } else {
                                noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
                                long successful = 0;
                                Note note = new Note(title.getText().toString(), description.getText().toString());
                                if (callingActivity.equals("Main")) {
                                    successful = noteDatabaseHelper.addNote(note);
                                } else {
                                    //handle edit note logic
                                    note.setTitle(title.getText().toString());
                                    note.setDescription(description.getText().toString());
                                    successful = noteDatabaseHelper.updateNoteById(note.getId(), note);
                                }
                                if (successful < 0) {
                                    Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                } else {
                                    finish();
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
    public void onClick(View view) {
//        noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
//        long success;
//        if(description.getText().toString().isEmpty()){
//            success = noteDatabaseHelper.addNote(new Note(title.getText().toString(),"No description provided"));
//        }
//        else{
//            success = noteDatabaseHelper.addNote(new Note(title.getText().toString(),description.getText().toString()));
//        }
//        if(success < 0){
//            Toast.makeText(getApplicationContext(),"Something went wrong...",Toast.LENGTH_SHORT).show();
//        }
//        else{
//            Toast.makeText(getApplicationContext(),"Added note successfully",Toast.LENGTH_SHORT).show();
//            finish();
//        }
        speechRecognizer.startListening(intentRecognizer);
    }
}
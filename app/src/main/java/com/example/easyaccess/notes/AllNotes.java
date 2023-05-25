package com.example.easyaccess.notes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

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
                        case "view": {
                            //handle edit note logic
                            if (parts.length > 1) {
                                //implement logic, check if note exists in list
                                Optional<Note> note = notes.stream().findFirst().filter(n -> n.getId() == Integer.parseInt(parts[1]));
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
                                //handle delete logic, if note exists in list
                                Optional<Note> note = notes.stream().findFirst().filter(n -> n.getId() == Integer.parseInt(parts[1]));
                                if (note.isPresent()) {
                                    noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
                                    if (noteDatabaseHelper.deleteNoteById(note.get().getId())) {
                                        Toast.makeText(getApplicationContext(), "Note deleted successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    }
                                    noteDatabaseHelper.close();
                                } else {
                                    Toast.makeText(getApplicationContext(), "No note with given ID found!", Toast.LENGTH_SHORT).show();
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
        noteDatabaseHelper = new NoteDatabaseHelper(getApplicationContext());
        notes.clear();
        notes.addAll(noteDatabaseHelper.getAllNotes());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

    }
}
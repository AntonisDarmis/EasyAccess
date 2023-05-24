package com.example.easyaccess.notes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AllNotes extends AppCompatActivity implements View.OnClickListener {
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ImageView voiceButton;
    private NoteDatabaseHelper noteDatabaseHelper;
    List<Note> notes = new ArrayList<>();

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
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);


        speechRecognizer.startListening(intentRecognizer);
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
                    switch(parts[0]){
                        case "back":{
                            finish();
                            break;
                        }
                        case "edit":{
                            //handle edit note logic
                            if(parts.length > 1){
                                //implement logic, check if note exists in list
                            }
                            break;
                        }
                        case "delete":{
                            if (parts.length > 1){
                                //handle delete logic, if note exists in list
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
package com.example.easyaccess.calls;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.easyaccess.Help;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class History extends AppCompatActivity implements View.OnClickListener{

    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;

    private ArrayList<Contact> contactList;

    private ImageView voiceButton;

    private RecyclerView recyclerView;

    private ContactAdapter adapter;

    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    private String command;

    private int recyclerPosition = 0;

    private String contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.history_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();
        adapter = new ContactAdapter(this, contactList);
        recyclerView.setAdapter(adapter);

        voiceButton = findViewById(R.id.history_voice);
        voiceButton.setOnClickListener(this);

        Intent intent = getIntent();
        contactName = intent.getStringExtra("Name");
        contactName = contactName.substring(0, 1).toUpperCase() + contactName.substring(1);
        getSupportActionBar().setTitle("Call history with: " + contactName);
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
                    if (parts[0].equals("scroll")) {
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
                    }
                    else if (parts[0].equals("back") || parts[0].equals("buck")){
                        finish();
                    }
                    else if(parts[0].equals("help")){
                        intent = new Intent(History.this, Help.class);
                        intent.putExtra("callingActivity","HistoryActivity");
                        startActivity(intent);
                    }
                    else if(parts[0].equals("explain")){
                        voiceButton.setEnabled(false);
                        showExplanationDialog();
                        voiceButton.setEnabled(true);
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
        getHistory(contactName);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(History.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(History.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity displays the recent calls history with a contact.\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }


    private void getHistory(String contactName) {
        contactList.clear();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String selection = CallLog.Calls.CACHED_NAME + " = ?";
            String[] selectionArguments = {contactName};
            Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, selection, selectionArguments, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                String callType = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                String callDate = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                //String photo = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                Date date = new Date(Long.parseLong(callDate));
                String dir = null;
                if (name != null) {
                    if (!(name.isEmpty())) {
                        int dirCode = Integer.parseInt(callType);
                        switch (dirCode) {
                            case CallLog.Calls.OUTGOING_TYPE:
                                dir = "OUTGOING";
                                break;
                            case CallLog.Calls.INCOMING_TYPE:
                                dir = "INCOMING";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                dir = "MISSED";
                                break;
                        }
                        String builder = date.toString().substring(0, 11) +
                                "- " +
                                dir;
                        int dur = Integer.parseInt(duration);

                        String total = "";
                        if(dur > 60){
                            int minutes = dur/60;
                            int seconds = dur % 60;
                            total = minutes + "m" + " " + seconds + "s";
                        }
                        else{
                            total = dur + "s";
                        }
                        Contact contact = new Contact(dir, date.toString().substring(0, 11) + "-" + total, "Drawable");
                        contact.setDir(dir);
                        contactList.add(contact);
                    }
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        speechRecognizer.startListening(intentRecognizer);
    }
}
package com.example.easyaccess.calls;

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
import android.util.Log;
import android.widget.ImageView;

import com.example.easyaccess.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class History extends AppCompatActivity {

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
}
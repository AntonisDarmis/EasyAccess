package com.example.easyaccess.sms;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Chat extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;
    private ArrayList<Message> messages;
    private ImageView voiceButton; //maybe do it as type bar
    private int recyclerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messages = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().setTitle("Chat history with: " + extras.get("name"));
        //set recycler view
        recyclerView = findViewById(R.id.chat_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages);
        recyclerView.setAdapter(chatAdapter);

        getConversation((int) extras.get("id"));

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
                        case "back":
                            finish();
                            break;
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

    @SuppressLint("Range")
    public void getConversation(int id) {
        messages.clear();
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"body", "address", "type", "date"};
        Uri uri = Uri.parse("content://mms-sms/conversations/" + id);
        Cursor cursor = cr.query(uri, projection, null, null, null);
        String currentDate = " ";
        if (cursor.moveToFirst()) {
            currentDate = millisToDate(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
        }
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
            //Long dateSent= cursor.getLong(cursor.getColumnIndexOrThrow("date_sent"));
            Long dateReceived = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            if (address != null && !(body.isEmpty())) {
                Message message = new Message();
                message.setMessage(body);
                message.setType(type);
                //check if sender belongs to contacts and get photo, either set default
                if (address.substring(1).matches("[0-9]+")) {
                    String[] fields = getContactFromNumber(address);
                    message.setName(fields[0]);
                    message.setProfileUrl(fields[1]);
                    // message = new Message(body,date,fields[0],fields[1],type);
                } else {
                    message.setProfileUrl(null);
                    //message = new Message(body,date,address,null,type);
                }
                String date = millisToDate(dateReceived);
                if (currentDate.substring(0, 10).equals(date.substring(0, 10))) {
                    message.setTime(date.substring(11, 16));
                } else {
                    message.setTime(date);
                }

                currentDate = date;
                messages.add(message);
            }
        }
        cursor.close();
        chatAdapter.notifyDataSetChanged();
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        }, 100);
    }

    private String[] getContactFromNumber(String number) {
        String[] fields = {"", null};
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        ContentResolver resolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Cursor cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI},
                    null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fields[0] = cursor.getString(0);
                    fields[1] = cursor.getString(1);
                    cursor.close();
                    return fields;
                }
            }
        }
        fields[0] = number;
        return fields;
    }

    public static String millisToDate(long currentTime) {
        String finalDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        Date date = calendar.getTime();
        finalDate = date.toString();
        return finalDate;
    }

}
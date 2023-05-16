package com.example.easyaccess.sms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.List;

public class SMS extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView recyclerView;

    private List<SMSConversation> conversationList;
    private ConversationsAdapter adapter;

    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;
    private ImageView voiceButton;
    private String command;
    private EditText filter;
    private int recyclerPosition = 0;

    private List<Integer> threadIDS = new ArrayList<>();

    // @Override
//    public void onResume() {
//        super.onResume();
//        getConversationIDS();
//        createConversationList();
//    }

    @Override
    /**
     * Fetches all the conversations when activity is created
     */ protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        recyclerView = findViewById(R.id.conversations_recycler);
        voiceButton = findViewById(R.id.sms_voice);
        voiceButton.setOnClickListener(this);

        filter = findViewById(R.id.sms_filter);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        conversationList = new ArrayList<>();

        adapter = new ConversationsAdapter(this, conversationList);
        recyclerView.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(SMS.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);


        } else {
            Log.d("Contact access permission", "permission is already granted");
            getConversationIDS();
            createConversationList();
        }

//        getConversationIDS();
//        createConversationList();
        Log.d("LIST COUNT SIZE:", String.valueOf(threadIDS.size()));
        // getSpecific(threadIDS.get(3));
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

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getConversationIDS();
                createConversationList();
            } else {

                Toast.makeText(getApplicationContext(), "Read SMS permission is not enabled.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Method to fetch all SMS thread IDS. Each conversation has its own unique ID, which will be used
     * to get the messages from each conversation separately
     *
     * @return a list of thread IDS
     */
    public void getConversationIDS() {

        ArrayList<String> sms = new ArrayList<>();
        Uri uriSMSURI = Uri.parse("content://mms-sms/conversations/");
        String[] projection = new String[]{"THREAD_ID"};
        Cursor cursor = getContentResolver().query(uriSMSURI, projection, null, null, "date desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int thread_ID = cursor.getInt(cursor.getColumnIndexOrThrow("THREAD_ID"));
                threadIDS.add(thread_ID);
            }
            cursor.close();
        }
    }


    /**
     * body is the message body, type = 1 means received, type = 2 means sent and address is the message's sender
     *
     * @param id, the thread ID of the selected conversation
     * @return an array list of the conversation's messages
     */
    @SuppressLint("Range")
    public SMSConversation getSpecific(int id) {
        ArrayList<String> sms = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"body", "address"};
        Uri uri = Uri.parse("content://mms-sms/conversations/" + id);
        Cursor cursor = cr.query(uri, projection, null, null, null);
        //only get last message
        if (cursor.moveToLast()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            if (address != null && !(body.isEmpty())) {
                SMSConversation smsConversation;
                //check if sender belongs to contacts and get photo, either set default
                body = formatString(body);
                if (address.substring(1).matches("[0-9]+")) {
                    String[] fields = getContactFromNumber(address);
                    smsConversation = new SMSConversation(fields[0], body, fields[1]);
                } else {
                    smsConversation = new SMSConversation(address, body, null);

                }
                cursor.close();
                return smsConversation;
            }
        }
        cursor.close();
        return null;
    }

    public void createConversationList() {
        conversationList.clear();
        for (int id : threadIDS) {
            SMSConversation conversation = getSpecific(id);
            if (conversation != null) {
                conversationList.add(conversation);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View view) {
        speechRecognizer.startListening(intentRecognizer);
    }

    private String formatString(String body) {

        if (body.contains("\n")) {
            body = body.replace("\n", "");
        }
        StringBuilder builder = new StringBuilder(body);
        if (body.length() > 36) {
            builder.setLength(36);
            builder.replace(builder.length() - 2, builder.length(), "...");
        }
        body = builder.toString();
        return body;
    }

    @SuppressLint("Range")
    private String[] getContactFromNumber(String number) {
        String[] fields = {"", null};
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        ContentResolver resolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Cursor cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI}, null, null);
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

}
package com.example.easyaccess.sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.SpeechRecognizer;
import android.widget.ImageView;

import com.example.easyaccess.R;
import com.example.easyaccess.calls.ContactAdapter;

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private SpeechRecognizer recognizer;
    private Intent intentRecognizer;
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

        getConversation((int)extras.get("id"));
    }

    @SuppressLint("Range")
    public void getConversation(int id) {
        messages.clear();
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"body", "address","type","date_sent"};
        Uri uri = Uri.parse("content://mms-sms/conversations/" + id);
        Cursor cursor = cr.query(uri, projection, null, null, null);
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date_sent"));
            if (address != null && !(body.isEmpty())) {
                Message message;
                //check if sender belongs to contacts and get photo, either set default
                if (address.substring(1).matches("[0-9]+")) {
                    String[] fields = getContactFromNumber(address);
                    message = new Message(body,date,fields[0],fields[1],type);
                } else {
                    message = new Message(body,date,address,null,type);
                }
                cursor.close();
                messages.add(message);
            }
        }
        cursor.close();
        chatAdapter.notifyDataSetChanged();
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

}
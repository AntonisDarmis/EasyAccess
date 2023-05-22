package com.example.easyaccess.sms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.LeveshteinDistance;
import com.example.easyaccess.R;
import com.example.easyaccess.calls.Contact;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SMS extends AppCompatActivity implements View.OnClickListener  {


    private RecyclerView recyclerView;
    private Map<String, String[]> contactCache = new HashMap<>(); // Cache to store already fetched contact information


    private List<SMSConversation> conversationList;

    private List<SMSConversation> conversationListFull;
    private ConversationsAdapter adapter;

    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;
    private ImageView voiceButton, searchIcon;
    private Button testButton;
    private String command;

    private ProgressBar loadingCircle;

    private EditText filter;
    private int recyclerPosition = 0;

    private Map<Integer, String> threadIDS = new HashMap<>();

    private SmsReceiver smsReceiver;



    @Override
    /**
     * Fetches all the conversations when activity is created
     */ protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);


        loadingCircle = findViewById(R.id.loading_circle);
        loadingCircle.setVisibility(View.VISIBLE);

        searchIcon = findViewById(R.id.searchIcon);
        searchIcon.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.conversations_recycler);
        voiceButton = findViewById(R.id.sms_voice);
        voiceButton.setOnClickListener(this);
        voiceButton.setVisibility(View.GONE);

        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(this);

        filter = findViewById(R.id.sms_filter);
        filter.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        conversationList = new ArrayList<>();
        conversationListFull = new ArrayList<>();

        adapter = new ConversationsAdapter(this, conversationList);
        recyclerView.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(SMS.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);

        } else {
            Log.d("SMS access permission", "permission is already granted");
            new Thread(() -> {
                getConversationIDS();
                createConversationList();
                conversationListFull.addAll(conversationList);
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    loadingCircle.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    filter.setVisibility(View.VISIBLE);
                    voiceButton.setVisibility(View.VISIBLE);
                    searchIcon.setVisibility(View.VISIBLE);
                });
            }).start();
        }

        if (ActivityCompat.checkSelfPermission(SMS.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);

        }

         smsReceiver = new SmsReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                Toast.makeText(getApplicationContext(), "New message received!", Toast.LENGTH_LONG).show();
                if (received) {
                    loadingCircle.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    new Thread(() -> {
                        getConversationIDS();
                        createConversationList();
                        conversationListFull.addAll(conversationList);
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            loadingCircle.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            filter.setVisibility(View.VISIBLE);
                            voiceButton.setVisibility(View.VISIBLE);
                            searchIcon.setVisibility(View.VISIBLE);
                        });
                    }).start();
                }
            }
        };

        registerSmsReceiver();



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
                        case "conversation": {
                            if (parts.length > 1) {
                                boolean found = false;
                                int id = -1;
                                String name = "";
                                for (Map.Entry<Integer, String> entry : threadIDS.entrySet()) {
                                    if (entry.getValue().equals(capitalize(parts[1]))) {
                                        found = true;
                                        id = entry.getKey();
                                        name = entry.getValue();
                                        break;
                                    }
                                }
                                if (found) {
                                    intent = new Intent(SMS.this, Chat.class);
                                    intent.putExtra("id", id);
                                    intent.putExtra("name", name);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
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
                        case "view": {
                            //perform Contact filtering
                            if (parts.length > 1) {
                                parts[1] = parts[1].replaceAll("[^a-z0-9]", "");
                                filter.setText(parts[1]);
                                filterList(parts[1]);
                                break;
                            }
                            break;
                        }
                        case "clear": {
                            conversationList.clear();
                            conversationList.addAll(conversationListFull);
                            adapter.notifyDataSetChanged();
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
        else{
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "Receive SMS permission is not enabled.", Toast.LENGTH_LONG).show();
                finish();
            }

        }
    }

    /**
     * Method to fetch all SMS thread IDS. Each conversation has its own unique ID, which will be used
     * to get the messages from each conversation separately
     *
     * @return a Map of thread IDS-contact names
     */
    public void getConversationIDS() {

        Uri uriSMSURI = Uri.parse("content://mms-sms/conversations/");
        String[] projection = new String[]{"THREAD_ID", "address"};
        Cursor cursor = getContentResolver().query(uriSMSURI, projection, null, null, "date desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int thread_ID = cursor.getInt(cursor.getColumnIndexOrThrow("THREAD_ID"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                threadIDS.put(thread_ID, address);
            }
            cursor.close();
            for (Map.Entry<Integer, String> thread : threadIDS.entrySet()) {
                String fields[] = getContactFromNumber(thread.getValue());
                threadIDS.replace(thread.getKey(), fields[0]);
            }
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
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"body", "address", "date"};
        Uri uri = Uri.parse("content://mms-sms/conversations/" + id);
        Cursor cursor = cr.query(uri, projection, null, null, null);
        //only get last message
        if (cursor.moveToLast()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            Long dateReceived = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            if (address != null && !(body.isEmpty())) {
                SMSConversation smsConversation;
                //check if sender belongs to contacts and get photo, either set default
                body = formatString(body);
                String date = millisToDate(dateReceived);
                if (address.substring(1).matches("[0-9]+")) {
                    String[] fields = getContactFromNumber(address);
                    smsConversation = new SMSConversation(fields[0], body, fields[1], date);
                } else {
                    smsConversation = new SMSConversation(address, body, null, date);
                }
                return smsConversation;
            }
            cursor.close();
        }
        cursor.close();
        return null;
    }

    public void createConversationList() {
        conversationList.clear();
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Map.Entry<Integer, String> thread : threadIDS.entrySet()) {
            SMSConversation conversation = getSpecific(thread.getKey());
            if (conversation != null) {
                conversationList.add(conversation);
            }
        }

        Collections.sort(conversationList, (c1, c2) -> {
            try {
                Date date1 = inputFormat.parse(c1.getDate());
                Date date2 = inputFormat.parse(c2.getDate());
                String formattedDate1 = outputFormat.format(date1);
                String formattedDate2 = outputFormat.format(date2);
                return formattedDate2.compareTo(formattedDate1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.testButton) {
            Intent intent = new Intent(SMS.this, Chat.class);
            boolean found = false;
            int id = -1;
            String name = "";
            for (Map.Entry<Integer, String> entry : threadIDS.entrySet()) {
                if (entry.getValue().equals("Mom")) {
                    found = true;
                    id = entry.getKey();
                    name = entry.getValue();
                    break;
                }
            }
            if (found) {
                intent = new Intent(SMS.this, Chat.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }
        } else {
            speechRecognizer.startListening(intentRecognizer);
        }
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
        if (contactCache.containsKey(number)) {
            return contactCache.get(number);
        }
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
                    contactCache.put(number, fields);
                    return fields;
                }
            }
        }
        fields[0] = number;
        return fields;
    }

    private static String capitalize(String str) {

        if (str == null || str.length() == 0) return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }

    private static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }


    private void filterList(String filter) {
        List<SMSConversation> filteredList = new ArrayList<>();
        for (SMSConversation sms : conversationList) {
            if (sms.getName().toLowerCase().contains(filter) || sms.getName().toLowerCase().equals(stripAccents(filter)) || sms.getMessage().contains(filter)
                    || LeveshteinDistance.computeLeveshteinDistance(sms.getName().toLowerCase(), filter) <= 1) {
                filteredList.add(sms);
            }
        }
        conversationList.clear();
        conversationList.addAll(filteredList);
        Collections.sort(conversationList, Comparator.comparing(SMSConversation::getName));
        adapter.notifyDataSetChanged();

    }

    private static String millisToDate(long currentTime) {
        String finalDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        Date date = calendar.getTime();
        finalDate = date.toString();
        return finalDate;
    }

    protected void onResume() {
        super.onResume();
        registerSmsReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSmsReceiver();
    }
    private void registerSmsReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

    private void unregisterSmsReceiver() {
        unregisterReceiver(smsReceiver);
    }



//    private int getKeyFromContactName(String name){
//        for (Map.Entry<Integer, String> thread : threadIDS.entrySet()) {
//            if(thread.getValue().equals(name)){
//                return thread.getKey();
//            }
//        }
//        return -1;
//    }

//    private List<SMSConversation> searchInConversationsByNameAndMessages(String keyword) {
//        List<SMSConversation> results = new ArrayList<>();
//        boolean bodyContains = false;
//        boolean nameContains = false;
//        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
//        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        ContentResolver cr = getContentResolver();
//        String[] projection = new String[]{"body", "address", "date"};
//        Uri uri = Uri.parse("content://mms-sms/conversations/");
//        Cursor cursor = cr.query(uri, projection, null, null, null);
//        while (cursor.moveToNext()) {
//            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
//            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
//            Long dateReceived = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
//            if (address != null && !(body.isEmpty())) {
//                SMSConversation smsConversation;
//                //check if sender belongs to contacts and get photo, either set default
//                body = formatString(body);
//                if (body.toLowerCase().contains(keyword)) {
//                    Log.d("BODY MATCH:",body + " " + keyword);
//                    bodyContains = true;
//                }
//                String date = millisToDate(dateReceived);
//                if (address.substring(1).matches("[0-9]+")) {
//                    String[] fields = getContactFromNumber(address);
//                    if (wordContains(fields[0], keyword)) {
//                        nameContains = true;
//                    }
//                    Log.d("CONTACT FOUND", fields[0] + " BODY: " + body);
//                    smsConversation = new SMSConversation(fields[0], body, fields[1], date);
//                } else {
//                    if (wordContains(address, keyword)) {
//                        nameContains = true;
//                    }
//                    Log.d("CONTACT FOUND", address + " BODY: " + body);
//                    smsConversation = new SMSConversation(address, body, null, date);
//                }
//                if (nameContains || bodyContains) {
//                    results.add(smsConversation);
//                }
//            }
//        }
//        cursor.close();
//        conversationList.clear();
//        conversationList.addAll(results);
//        Collections.sort(conversationList, (c1, c2) -> {
//            try {
//                Date date1 = inputFormat.parse(c1.getDate());
//                Date date2 = inputFormat.parse(c2.getDate());
//                String formattedDate1 = outputFormat.format(date1);
//                String formattedDate2 = outputFormat.format(date2);
//                return formattedDate2.compareTo(formattedDate1);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            return 0;
//        });
//        adapter.notifyDataSetChanged();
//        return results;
//    }
//
//    private boolean wordContains(String searchedWord, String keyword) {
//        if (searchedWord.toLowerCase().contains(keyword) || searchedWord.toLowerCase().equals(stripAccents(keyword))
//                || LeveshteinDistance.computeLeveshteinDistance(searchedWord.toLowerCase(), keyword) <= 1) {
//            return true;
//        }
//        return false;
//    }
}


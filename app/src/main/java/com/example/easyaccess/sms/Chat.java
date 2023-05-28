package com.example.easyaccess.sms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.Help;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.R;
import com.example.easyaccess.calls.Calls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Chat extends AppCompatActivity implements View.OnClickListener {

    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;

    private RecyclerView recyclerView;

    private TextView resultCounter;
    private TextView count;
    private ChatAdapter chatAdapter;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;
    private ArrayList<Message> messages;

    private SmsReceiver smsReceiver;
    private ImageView voiceButton; //maybe do it as type bar
    private int recyclerPosition = 0;

    private String profileURL, name;

    private int currentMatchIndex = -1;

    private int position = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private int conversationID;

    private EditText messageInput;

    private ArrayList<Integer> matchPositions = new ArrayList<>();

    private String number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messages = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().setTitle("Chat history with: " + extras.get("name"));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }

        messageInput = findViewById(R.id.messageInput);
        //set recycler view
        recyclerView = findViewById(R.id.chat_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages);
        recyclerView.setAdapter(chatAdapter);

        voiceButton = findViewById(R.id.sms_chat);
        voiceButton.setOnClickListener(this);
        conversationID = extras.getInt("id");

        getConversation(conversationID);


        resultCounter = findViewById(R.id.resultCounter);
        count = findViewById(R.id.count);
        resultCounter.setVisibility(View.GONE);
        count.setVisibility(View.GONE);

        smsReceiver = new SmsReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                Toast.makeText(getApplicationContext(), "New message received!", Toast.LENGTH_LONG).show();
                if (received) {
                    getLastMessage();
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
                        case "back": {
                            finish();
                            break;
                        }
                        case "search": {
                            if (parts.length > 1) {
                                searchMessages(parts[1]);
                            }
                            break;
                        }
                        case "next": {
                            if (!matchPositions.isEmpty()) {
                                navigateToNextMatch();
                                break;
                            }
                        }
                        case "previous": {
                            if (!matchPositions.isEmpty()) {
                                navigateToPreviousMatch();
                                break;
                            }
                        }
                        case "clear": {
                            resultCounter.setVisibility(View.GONE);
                            count.setVisibility(View.GONE);
                            chatAdapter.clearHighlightedItem();
                            recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.scrollToPosition(messages.size() - 1);
                                }
                            }, 100);
                        }
                        case "message": {
                            if (parts.length > 1) {
                                messageInput.setText(parts[1]);
                                break;
                            }
                            break;
                        }
                        case "send": {
                            if (!(messageInput.getText().toString().isEmpty())) {
                                sendSmsMessage();
                                messageInput.setText("");
                                break;
                            }
                            break;
                        }
                        case "help":{
                            intent = new Intent(Chat.this, Help.class);
                            intent.putExtra("callingActivity","ChatActivity");
                            startActivity(intent);
                            break;
                        }
                        case "explain": {
                            voiceButton.setEnabled(false);
                            showExplanationDialog();
                            voiceButton.setEnabled(true);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(Chat.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality of a SMS conversation with a contact. By using the correct commands you can search for a " +
                            "message in the conversation, send and receive messages and view the entire conversation.\n Say 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
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
            number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
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
        recyclerView.scrollToPosition(messages.size() - 1);
    }


    @SuppressLint("Range")
    private void getLastMessage() {
        Uri uri = Uri.parse("content://mms-sms/conversations/" + conversationID);
        String[] projection = new String[]{"body", "address", "type", "date"};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, "date DESC LIMIT 1");
        String currentDate;
        if (cursor != null && cursor.moveToFirst()) {
            String body = cursor.getString(cursor.getColumnIndex("body"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            int type = cursor.getInt(cursor.getColumnIndex("type"));
            long date = cursor.getLong(cursor.getColumnIndex("date"));
            currentDate = millisToDate(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
            Message message = new Message();
            message.setMessage(body);
            message.setType(type);
            message.setTime(currentDate);
            message.setName(name);
            message.setProfileUrl(profileURL);
            messages.add(message);
            chatAdapter.notifyDataSetChanged();
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            }, 100);
        }
    }


    private void searchMessages(String keyword) {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.getMessage().toLowerCase().contains(keyword)) {
                Log.d("POSITION: ", String.valueOf(i));
                matchPositions.add(i);
            }
        }
        if (!matchPositions.isEmpty()) {
            // Reset current match index to the first match
            resultCounter.setVisibility(View.VISIBLE);
            count.setVisibility(View.VISIBLE);
            currentMatchIndex = 0;
            position = 1;
            resultCounter.setText(keyword);
            String counter = position + "/" + matchPositions.size() + " results found.";
            count.setText(counter);
            scrollToMatch(currentMatchIndex);

        } else {
            chatAdapter.clearHighlightedItem();
        }
    }

    private void scrollToMatch(int matchIndex) {
        if (matchIndex >= 0 && matchIndex < matchPositions.size()) {
            int position = matchPositions.get(matchIndex);
            recyclerView.scrollToPosition(position);
            chatAdapter.setHighlightedItem(position);
            currentMatchIndex = matchIndex;
        }
    }

    private void navigateToNextMatch() {
        if (currentMatchIndex < matchPositions.size() - 1) {
            scrollToMatch(currentMatchIndex + 1);
            position = position + 1;
            String counter = position + "/" + matchPositions.size() + " results found.";
            count.setText(counter);
        }
    }

    private void navigateToPreviousMatch() {
        if (currentMatchIndex > 0) {
            scrollToMatch(currentMatchIndex - 1);
            position = position - 1;
            String counter = position + "/" + matchPositions.size() + " results found.";
            count.setText(counter);
        }
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
                    profileURL = fields[1];
                    name = fields[0];
                    cursor.close();
                    return fields;
                }
            }
        }
        fields[0] = number;
        name = number;
        profileURL = null;
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

    @Override
    public void onClick(View view) {
        speechRecognizer.startListening(intentRecognizer);
    }

    private void sendSmsMessage() {
        String message = messageInput.getText().toString();
        String phoneNumber = number;

        // Create a PendingIntent for the SMS sending result
        Intent sentIntent = new Intent("SMS_SENT");
        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create a BroadcastReceiver to handle the result of the SMS sending operation
        BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // SMS sent successfully
                        Toast.makeText(getApplicationContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        getLastMessage();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // Failed to send SMS
                        Toast.makeText(getApplicationContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // No service available to send SMS
                        Toast.makeText(getApplicationContext(), "No service available to send SMS", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // Null PDU error
                        Toast.makeText(getApplicationContext(), "Null PDU error", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // Radio off error
                        Toast.makeText(getApplicationContext(), "Radio off error", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        // Register the BroadcastReceiver to receive the SMS sending result
        registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"));

        // Send the SMS message with the sentPendingIntent
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, sentPendingIntent, null);
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {// If request is cancelled, the result arrays are empty.
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "Send SMS permission is not enabled.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
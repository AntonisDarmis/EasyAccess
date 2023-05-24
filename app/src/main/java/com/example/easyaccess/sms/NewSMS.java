package com.example.easyaccess.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.Locale;

public class NewSMS extends AppCompatActivity implements View.OnClickListener {

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private ImageView voiceButton;
    private Button testButton;
    private String command;
    private TextView recipient;

    private EditText input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sms);
        voiceButton = findViewById(R.id.newSMS_voice);
        voiceButton.setOnClickListener(this);

        recipient = findViewById(R.id.newSMS_number);
        recipient.setText(new Intent().getStringExtra("NUMBER"));

        input = findViewById(R.id.newSMS_input);

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
                        case "clear": {
                            input.setText("");
                            break;
                        }
                        case "back": {
                            finish();
                            break;
                        }
                        case "message": {
                            if (parts.length > 1) {
                                input.setText(parts[1]);
                            }
                            break;
                        }
                        case "send": {
                            if (input.getText().length() != 0) {
                                sendSMS();
                                finish();
                                break;
                            }
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

    private void sendSMS() {
        String message = input.getText().toString();
        String phoneNumber = recipient.getText().toString();

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
        registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"));
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, sentPendingIntent, null);
        Toast.makeText(getApplicationContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        speechRecognizer.startListening(intentRecognizer);
    }
}
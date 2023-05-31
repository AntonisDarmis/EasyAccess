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
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.ExplanationDialogHelper;
import com.example.easyaccess.Help;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.R;
import com.example.easyaccess.calls.Calls;
import com.example.easyaccess.reminders.Reminder;

import java.util.ArrayList;
import java.util.Locale;

public class NewSMS extends AppCompatActivity implements View.OnClickListener {

    private PopupWindow popupWindow;
    private TextView messageTextView;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private ImageView voiceButton;
    private Button testButton;
    private String command;
    private TextView recipient;

    private EditText input;

    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewSMS.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(NewSMS.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality for sending an SMS message to a new number." +
                            "\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sms);
        voiceButton = findViewById(R.id.newSMS_voice);
        voiceButton.setOnClickListener(this);

        recipient = findViewById(R.id.newSMS_number);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        recipient.setText(extras.get("NUMBER").toString());

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
                popupWindow.dismiss();
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
                            popupWindow.dismiss();
                            input.setText("");
                            break;
                        }
                        case "buck":
                        case "back": {
                            popupWindow.dismiss();
                            finish();
                            break;
                        }
                        case "message": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                input.setText(parts[1]);
                            }
                            break;
                        }
                        case "send": {
                            popupWindow.dismiss();
                            if (input.getText().length() != 0) {
                                sendSMS();
                                finish();
                                break;
                            }
                        }
                        case "help":{
                            popupWindow.dismiss();
                            intent = new Intent(NewSMS.this, Help.class);
                            intent.putExtra("callingActivity","NewSMSActivity");
                            startActivity(intent);
                            break;
                        }
                        case "explain": {
                            popupWindow.dismiss();
                            voiceButton.setEnabled(false);
                            showExplanationDialog();
                            voiceButton.setEnabled(true);
                            break;
                        }
                    }
                    popupWindow.dismiss();
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
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // Create the popup window
        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Find the TextView in the popup layout
        messageTextView = popupView.findViewById(R.id.messageTextView);

        // Show the popup window at the center of the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        speechRecognizer.startListening(intentRecognizer);

    }
}
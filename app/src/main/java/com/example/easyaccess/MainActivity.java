package com.example.easyaccess;

import static android.Manifest.permission.RECORD_AUDIO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.easyaccess.calls.Calls;
import com.example.easyaccess.sms.SMS;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SpeechRecognizer speechRecognizer;
    private Button button;
    private Intent intentRecognizer;
    private String command;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);



        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
       // intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-gr");
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
                if(matches != null)
                {
                    command = matches.get(0);
                    command = command.toLowerCase(Locale.ROOT);
                    textView.setText(command);
                    Intent intent;
                    switch(command)
                    {
                        case "contact":
                        {

                            //open calls activity
                            intent = new Intent(MainActivity.this, Calls.class);
                            startActivity(intent);

                            break;
                        }
                        case "maps":
                        {
                            //open maps activity
                            intent = new Intent();
                            break;
                        }
                        case "sms":
                        {
                            //open sms activity
                            intent = new Intent(MainActivity.this, SMS.class);
                            startActivity(intent);
                            String test ="";
                            break;
                        }
                        case "help":
                        {
                            //prompt help toolbox with text to speech bot??
                            break;
                        }
                        case "alert":
                        {
                            //call emergency contact
                            break;
                        }
                        case "camera":
                        {
                            break;
                        }
                        case "settings":
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

    @Override
    public void onClick(View view) {
        //speechRecognizer.startListening(intentRecognizer);
        Intent intent = new Intent(this, SMS.class);
        startActivity(intent);
        finish();
    }

    public void openApplication(String app)
    {
        return;
    }
}
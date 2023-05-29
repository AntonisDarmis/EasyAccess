package com.example.easyaccess.maps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DirectionsActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView voiceButton;

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;

    private RadioGroup transportOptions;

    private LinearLayout routeOptions;

    private String transportOption;


    private String command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        voiceButton = findViewById(R.id.directions_voice);
        voiceButton.setOnClickListener(this);

        findViewById(R.id.textView19).setVisibility(View.GONE);

        transportOptions = findViewById(R.id.radioGroupTransport);

        routeOptions = findViewById(R.id.routeOptions);
        routeOptions.setVisibility(View.GONE);
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
                    switch (parts[0]) {
                        case "route": {
                            ((TextView) findViewById(R.id.route)).setText(parts[0].substring(0, 1).toUpperCase(Locale.ROOT) + parts[0].substring(1));
                            break;
                        }
                        case "transport": {
                            if (parts.length > 1) {
                                switch (parts[1]) {
                                    case "car": {
                                        transportOptions.check(R.id.radioButton_car);
                                        break;
                                    }
                                    case "public transport": {
                                        transportOptions.check(R.id.radioButton_public_transport);
                                        break;
                                    }
                                    case "on foot": {
                                        transportOptions.check(R.id.radioButton_on_foot);
                                        break;
                                    }
                                }
                                setRouteOptions();
                            }
                        }
                        case "directions": {
                            if (transportOption.equals("car")) {
                                openDestinationByCar();
                            } else if (transportOption.equals("public transport")) {
                                openDestinationByPublicTransport();
                            } else if (transportOption.equals("on foot")) {
                                openDestinationByWalking();
                            }
                            break;
                        }
                    }
                    // set check boxes based on command
                    if (transportOption.equals("car")) {
                        switch (command) {
                            case "avoid tolls": {
                                if (((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).isChecked()) {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).setChecked(false);
                                } else {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).setChecked(true);
                                }
                                break;
                            }
                            case "avoid motorways": {
                                if (((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).isChecked()) {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).setChecked(false);
                                } else {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).setChecked(true);
                                }
                                break;
                            }
                            case "avoid ferries": {
                                if (((CheckBox) routeOptions.findViewById(R.id.checkBox_option3)).isChecked()) {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option3)).setChecked(false);
                                } else {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option3)).setChecked(true);
                                }
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

    private void setRouteOptions() {
        int selectedId = transportOptions.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
        if (selectedRadioButton.getText().toString().equals("Car")) {
            transportOption = "car";
            routeOptions.setVisibility(View.VISIBLE);
            ((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).setText("Avoid tolls");
            ((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).setText("Avoid motorways");
            ((CheckBox) routeOptions.findViewById(R.id.checkBox_option3)).setText("Avoid ferries");
        } else if (selectedRadioButton.getText().toString().equals("Public transport")) {
            transportOption = "public transport";
            routeOptions.setVisibility(View.GONE);
            findViewById(R.id.textView19).setVisibility(View.VISIBLE);
        } else if (selectedRadioButton.getText().toString().equals("On foot")) {
            transportOption = "on foot";
            routeOptions.setVisibility(View.GONE);
            findViewById(R.id.textView19).setVisibility(View.GONE);
        }
    }


    private void openDestinationByCar() {
        String destination = ((TextView) findViewById(R.id.route)).getText().toString();  // Replace with the destination address or coordinates
        Log.d("DESTINATION", destination);
        String uri = "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=" + destination;
        CheckBox option1, option2, option3;
        option1 = routeOptions.findViewById(R.id.checkBox_option1);
        option2 = routeOptions.findViewById(R.id.checkBox_option2);
        option3 = routeOptions.findViewById(R.id.checkBox_option3);
        boolean avoidTolls = option1.isChecked();
        boolean avoidMotorways = option2.isChecked();
        boolean avoidFerries = option3.isChecked();

        if (avoidTolls && avoidMotorways && avoidFerries) {
            uri += "&avoid=tolls|highways|ferries";
        } else if (avoidTolls && avoidMotorways) {
            uri += "&avoid=tolls|highways";
        } else if (avoidTolls && avoidFerries) {
            uri += "&avoid=tolls|ferries";
        } else if (avoidMotorways && avoidFerries) {
            uri += "&avoid=highways|ferries";
        } else if (avoidTolls) {
            uri += "&avoid=tolls";
        } else if (avoidMotorways) {
            uri += "&avoid=highways";
        } else if (avoidFerries) {
            uri += "&avoid=ferries";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");  // Specify the package to ensure it opens in Maps app

        // Check if there is a Maps app installed on the device
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start the activity if there is a Maps app installed
        if (isIntentSafe) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Google Maps is not installed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDestinationByPublicTransport() {
        String destination = ((TextView) findViewById(R.id.route)).getText().toString();
        String uri = "https://www.google.com/maps/dir/?api=1&travelmode=transit&destination=" + destination;
        uri += "&transit_routing_preference=accessible";
        // Create the intent to open Maps
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");  // Specify the package to ensure it opens in Maps app

        // Check if there is a Maps app installed on the device
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start the activity if there is a Maps app installed
        if (isIntentSafe) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Google Maps is not installed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDestinationByWalking() {
        String destination = ((TextView) findViewById(R.id.route)).getText().toString();
        String uri = "https://www.google.com/maps/dir/?api=1&travelmode=walking&destination=" + destination;
        uri += "&dir_action=w";
        // Create the intent to open Maps
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");  // Specify the package to ensure it opens in Maps app

        // Check if there is a Maps app installed on the device
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start the activity if there is a Maps app installed
        if (isIntentSafe) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Google Maps is not installed!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View view) {
        speechRecognizer.startListening(intentRecognizer);
    }
}
package com.example.easyaccess.maps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easyaccess.ExplanationDialogHelper;
import com.example.easyaccess.MainActivity;
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

    private PopupWindow popupWindow;
    private TextView messageTextView;
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(DirectionsActivity.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(DirectionsActivity.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality of getting and viewing directions to a certain address or area." +
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
                messageTextView.setText("Processing");
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
                    messageTextView.setText(command);
                    command = command.toLowerCase(Locale.ROOT);
                    String[] parts = command.split(" ", 2);
                    Log.d("VOICE COMMAND IN ADD", command);
                    switch (parts[0]) {
                        case "buck":
                        case "back":{
                            popupWindow.dismiss();
                            finish();
                            break;
                        }
                        case "destination": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                ((TextView) findViewById(R.id.route)).setText(parts[1].substring(0, 1).toUpperCase(Locale.ROOT) + parts[1].substring(1));
                            }
                            break;
                        }
                        case "start": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                ((TextView) findViewById(R.id.start)).setText(parts[1].substring(0, 1).toUpperCase(Locale.ROOT) + parts[1].substring(1));
                            }
                            break;
                        }
                        case "transport": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                if (parts[1].equals("car")) {
                                    transportOptions.check(R.id.radioButton_car);
                                    setRouteOptions();
                                } else if (parts[1].equals("public transport")) {
                                    transportOptions.check(R.id.radioButton_public_transport);
                                    setRouteOptions();
                                } else if (parts[1].equals("on foot")) {
                                    transportOptions.check(R.id.radioButton_on_foot);
                                    setRouteOptions();
                                }
                            }
                            break;
                        }
                        case "directions": {
                            popupWindow.dismiss();
                            if (transportOption.equals("car")) {
                                openDestinationByCar();
                            } else if (transportOption.equals("public transport")) {
                                openDestinationByPublicTransport();
                            } else if (transportOption.equals("on foot")) {
                                openDestinationByWalking();
                            }
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
                    // set check boxes based on command
                    if (transportOption != null) {
                        popupWindow.dismiss();
                        switch (command) {
                            case "avoid tolls": {
                                if (((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).isChecked()) {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).setChecked(false);
                                } else {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).setChecked(true);
                                }
                                break;
                            }
                            case "avoid motorway": {
                                if (((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).isChecked()) {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).setChecked(false);
                                } else {
                                    ((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).setChecked(true);
                                }
                                break;
                            }
                            case "avoid ferry": {
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
        RadioButton selectedRadioButton = findViewById(selectedId);
        if (selectedRadioButton.getText().toString().equals("Car")) {
            transportOption = "car";
            routeOptions.setVisibility(View.VISIBLE);
            ((CheckBox) routeOptions.findViewById(R.id.checkBox_option1)).setText("Avoid tolls");
            ((CheckBox) routeOptions.findViewById(R.id.checkBox_option2)).setText("Avoid motorway");
            ((CheckBox) routeOptions.findViewById(R.id.checkBox_option3)).setText("Avoid ferry");
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
        String start = ((TextView) findViewById(R.id.start)).getText().toString();
        String destination = ((TextView) findViewById(R.id.route)).getText().toString();
        String uri = "";
        if (start.isEmpty()) {
            uri = "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=" + destination;
        } else {
            uri = "https://www.google.com/maps/dir/?api=1&travelmode=driving&origin=" + start + "&destination=" + destination;
        }

        Log.d("START", start);
        Log.d("DESTINATION", destination);

        CheckBox option1, option2, option3;
        option1 = routeOptions.findViewById(R.id.checkBox_option1);
        option2 = routeOptions.findViewById(R.id.checkBox_option2);
        option3 = routeOptions.findViewById(R.id.checkBox_option3);
        boolean avoidTolls = option1.isChecked();
        boolean avoidMotorways = option2.isChecked();
        boolean avoidFerries = option3.isChecked();

        StringBuilder avoidParams = new StringBuilder();

        if (avoidTolls) {
            avoidParams.append("tolls");
        }

        if (avoidMotorways) {
            if (avoidParams.length() > 0) {
                avoidParams.append("|");
            }
            avoidParams.append("highways");
        }

        if (avoidFerries) {
            if (avoidParams.length() > 0) {
                avoidParams.append("|");
            }
            avoidParams.append("ferries");
        }

        if (avoidParams.length() > 0) {
            uri += "&avoid=" + avoidParams.toString();
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
        String start = ((TextView) findViewById(R.id.start)).getText().toString();
        String destination = ((TextView) findViewById(R.id.route)).getText().toString();
        String uri = "";
        if (start.isEmpty()) {
            uri = "https://www.google.com/maps/dir/?api=1&travelmode=transit&destination=" + destination;
        } else {
            uri = "https://www.google.com/maps/dir/?api=1&travelmode=transit&origin=" + start + "&destination=" + destination;
        }
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
        String start = ((TextView) findViewById(R.id.start)).getText().toString();
        String destination = ((TextView) findViewById(R.id.route)).getText().toString();
        String uri = "";
        if (start.isEmpty()) {
            uri = "https://www.google.com/maps/dir/?api=1&travelmode=walking&destination=" + destination;
        } else {
            uri = "https://www.google.com/maps/dir/?api=1&travelmode=walking&origin=" + start + "&destination=" + destination;
        }
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
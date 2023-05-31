package com.example.easyaccess.maps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.Help;
import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CategoriesActivity extends AppCompatActivity implements View.OnClickListener {
    List<String> selectedCategories = new ArrayList<>();
    String[] categories = {"restaurants", "bars", "coffee", "parks", "nightlife",
            "movies", "museums", "atms", "gas", "hospitals", "pharmacies",
            "groceries", "shopping centers"};
    private ImageView voiceButton;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private String command;
    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private int recyclerPosition = 0;

    private List<GeneralCategory> allCategories = new ArrayList<>();
    private PopupWindow popupWindow;
    private TextView messageTextView;
    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;

    private String currentLocation;

    private TextView destination;


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
                    AlertDialog.Builder builder = new AlertDialog.Builder(CategoriesActivity.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(CategoriesActivity.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality of viewing stores based on given categories around a provided location." +
                            ".\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        getCurrentLocation();

        destination = findViewById(R.id.categoriesRoute);

        voiceButton = findViewById(R.id.categories_voice);
        voiceButton.setOnClickListener(this);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<GeneralCategory> generalCategories = prepareData(); // Prepare the data for general categories

        categoryAdapter = new CategoryAdapter(generalCategories);
        categoryRecyclerView.setAdapter(categoryAdapter);

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
                messageTextView.setText("Processing...");
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
                        case "scroll": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                if (parts[1].equals("down")) {
                                    recyclerPosition += 3;
                                } else {
                                    recyclerPosition -= 3;
                                    if (recyclerPosition < 0) recyclerPosition = 0;
                                }
                                categoryRecyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        categoryRecyclerView.smoothScrollToPosition(recyclerPosition);
                                    }
                                }, 500);
                                break;
                            }
                        }
                        case "destination": {
                            popupWindow.dismiss();
                            if (parts.length > 1) {
                                destination.setText(capitalize(parts[1]));
                            }
                            break;
                        }
                        case "buck":
                        case "back": {
                            popupWindow.dismiss();
                            finish();
                            break;
                        }
                        case "help": {
                            popupWindow.dismiss();
                            Intent intent = new Intent(CategoriesActivity.this, Help.class);
                            intent.putExtra("callingActivity", "CategoriesActivity");
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
                        case "open": {
                            popupWindow.dismiss();
                            if (currentLocation != null && (destination.getText().toString().isEmpty())) {
                                openMapsWithCategories(String.valueOf(currentLocation));
                            }
                            else{
                                openMapsWithCategories(destination.getText().toString());
                            }
                            break;
                        }
                        default: {
                            popupWindow.dismiss();
                            if (Arrays.stream(categories).anyMatch(str -> str.equals(command))) {
                                updateSubCategoryImage(capitalize(command));
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

    private static final String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }




    private void openMapsWithCategories(String location) {
        // Create the URI with the location and search query
        StringBuilder uri = new StringBuilder("geo:0,0?q=" + Uri.encode(location));

        for (String category : getCheckedCategories()) {
            uri.append(",").append(Uri.encode(category));
        }

        // Create the intent to search for categories
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));

        // Check if there is an app available to handle the intent
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start the activity if there is an app available
        if (isIntentSafe) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
        }
    }


    private List<String> getCheckedCategories() {
        List<String> checkedCategories = new ArrayList<>();
        for (GeneralCategory generalCategory : allCategories) {
            for (SubCategory subCategory : generalCategory.getSubCategories()) {
                if (subCategory.isChecked()) {
                    Log.d("SUBCATEGORY", subCategory.getTitle());
                    checkedCategories.add(subCategory.getTitle());
                }
            }
        }
        return checkedCategories;
    }

    private List<GeneralCategory> prepareData() {
        List<GeneralCategory> generalCategories = new ArrayList<>();
        // Category Food and drinks
        List<SubCategory> foodAndDrinks = new ArrayList<>();
        foodAndDrinks.add(new SubCategory("Restaurants"));
        foodAndDrinks.add(new SubCategory("Bars"));
        foodAndDrinks.add(new SubCategory("Coffee"));
        generalCategories.add(new GeneralCategory("Food&Drinks", foodAndDrinks));

        // Category Things to do
        List<SubCategory> thingsToDo = new ArrayList<>();
        thingsToDo.add(new SubCategory("Parks"));
        thingsToDo.add(new SubCategory("Nightlife"));
        thingsToDo.add(new SubCategory("Movies"));
        thingsToDo.add(new SubCategory("Museums"));
        generalCategories.add(new GeneralCategory("Things to do", thingsToDo));

        //Category Services
        List<SubCategory> services = new ArrayList<>();
        services.add(new SubCategory("ATMs"));
        services.add(new SubCategory("Gas"));
        services.add(new SubCategory("Hospitals"));
        services.add(new SubCategory("Pharmacies"));
        generalCategories.add(new GeneralCategory("Services", services));

        //Category shopping
        List<SubCategory> shopping = new ArrayList<>();
        shopping.add(new SubCategory("Groceries"));
        shopping.add(new SubCategory("Shopping centers"));
        generalCategories.add(new GeneralCategory("Shopping", shopping));
        allCategories.addAll(generalCategories);
        return generalCategories;
    }

    private void updateSubCategoryImage(String subCategory) {
        // Find the corresponding GeneralCategory object in the list
        for (GeneralCategory category : allCategories) {
            // Find the corresponding SubCategory object in the general category
            for (SubCategory subCat : category.getSubCategories()) {
                if (subCat.getTitle().equalsIgnoreCase(subCategory)) {
                    // Set the new image for the subcategory
                    if (subCat.isChecked()) {
                        subCat.setChecked(false);
                    } else {
                        subCat.setChecked(true);
                    }
                }
            }
        }
        // Notify the adapter that the data has changed
        categoryAdapter.notifyDataSetChanged();
    }


    private void getCurrentLocation() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }

        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a location listener to handle location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Check if the location is available
                if (location != null) {
                    // Handle the retrieved location
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    currentLocation = latitude + "," + longitude; // Store the location in the global variable
                    Log.d("Current Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                } else {
                    Log.d("Current Location", "Location unavailable");
                }

                // Stop receiving location updates
                locationManager.removeUpdates(this);
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Request location updates
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
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
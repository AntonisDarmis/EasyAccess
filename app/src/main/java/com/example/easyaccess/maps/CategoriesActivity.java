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
import android.util.Log;
import android.view.Gravity;
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

import com.example.easyaccess.ExplanationDialogHelper;
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

    private static final String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

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
                    Log.d("VOICE COMMAND IN ADD", command);
                    if (command.startsWith("scroll")) {
                        if (command.equals("scroll up")) {
                            recyclerPosition -= 3;
                            if (recyclerPosition < 0) recyclerPosition = 0;
                        } else {
                            recyclerPosition += 3;
                        }
                        categoryRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                categoryRecyclerView.smoothScrollToPosition(recyclerPosition);
                            }
                        }, 500);
                    } else if (command.equals("destination")) {
                        ((TextView) findViewById(R.id.categoriesRoute)).setText(capitalize(command));

                    } else if (command.equals("back")) {
                        finish();
                    } else if (command.equals("explain")) {
                        voiceButton.setEnabled(false);
                        ExplanationDialogHelper dialogHelper = new ExplanationDialogHelper(getApplicationContext());
                        String dialogMessage = "This activity serves the functionality of viewing stores based on given categories around a provided location." +
                                ".\nSay 'HELP' to view the available commands!";
                        dialogHelper.showExplanationDialog(dialogMessage);
                        dialogHelper.shutdown();
                        voiceButton.setEnabled(true);
                    } else if (Arrays.stream(categories).anyMatch(str -> str.equals(command))) {
                        updateSubCategoryImage(capitalize(command));
                    } else if (command.equals("show")) {
                        setLocation(((TextView) findViewById(R.id.categoriesRoute)).getText().toString());
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

    private void setLocation(String location) {
        if (location.isEmpty()) {
            // Request location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                finish();

                return;
            }
            // Get the location manager
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Request a single location update
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Handle the location update
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    openMapsWithCategories(String.valueOf(latitude) + "," + String.valueOf(longitude));
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
            }, null);
        } else {
            openMapsWithCategories(capitalize(location));
        }
    }

    private void openMapsWithCategories(String location) {
        // Create the URI with the search query
        StringBuilder uri = new StringBuilder("geo:" + location + "?q=");

        for (String category : getCheckedCategories()) {
            uri.append(Uri.encode(category)).append(",");
        }

        // Remove the trailing comma
        uri = new StringBuilder(uri.substring(0, uri.length() - 1));

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
                    break;
                }
            }
            break;
        }
        // Notify the adapter that the data has changed
        categoryAdapter.notifyDataSetChanged();
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
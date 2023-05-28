package com.example.easyaccess.calls;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easyaccess.DialogFormatter;
import com.example.easyaccess.Help;
import com.example.easyaccess.LeveshteinDistance;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;
import java.util.Optional;

public class Calls extends AppCompatActivity implements View.OnClickListener {

    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;
    private RecyclerView recyclerView;
    private List<Contact> contactList;
    private ContactAdapter adapter;

    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    private ImageView voiceButton;

    private String command;
    private EditText filter;
    private int recyclerPosition = 0;

    private boolean isOnDeleteStage = false;

    private String deleteContactName;


    @Override
    public void onResume() {
        super.onResume();
        getContacts();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);
        recyclerView = findViewById(R.id.calls_recycler);

        voiceButton = findViewById(R.id.call_image);
        voiceButton.setOnClickListener(this);

        filter = findViewById(R.id.filter);
        filter.setPadding(2, 0, 0, 0);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        contactList = new ArrayList<>();


        if (ActivityCompat.checkSelfPermission(Calls.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);

        } else {
            Log.d("Contact access permission", "permission is already granted");

            getContacts();
            // voiceButton.setEnabled(true);

        }

        if (ActivityCompat.checkSelfPermission(Calls.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            Log.d("Phone call permission", "permission is already granted");
        }

        if (ActivityCompat.checkSelfPermission(Calls.this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 2);

//        } else {
//            Log.d("Contact access permission", "permission is already granted");
//            getCallLogs();
//            // voiceButton.setEnabled(true);

        }

        if (ActivityCompat.checkSelfPermission(Calls.this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 3);

        } else {
            Log.d("Contact access permission", "permission is already granted");

            // voiceButton.setEnabled(true);

        }


        Log.d("CONTACTS SIZE: ", "SIZE IS " + contactList.size());


        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        // intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-gr");

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
                        case "create": {
                            intent = new Intent(Calls.this, AddContact.class);
                            intent.putExtra("Request", "create");
                            startActivity(intent);
                            break;
                        }
                        case "edit": {
                            //PUT EXTRA contact name in Intent!
                            if (parts.length > 1) {
                                intent = new Intent(Calls.this, AddContact.class);
                                Contact contact;
                                contact = findByName(contactList, parts[1]);
                                if (contact != null) {
                                    intent.putExtra("Contact", contact);
                                    intent.putExtra("Request", "edit");
                                    startActivity(intent);
                                    break;
                                } else {
                                    Toast.makeText(getApplicationContext(), "Could not find a contact with given name.", Toast.LENGTH_LONG).show();
                                }
                            }
                            break;
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

                        case "delete": {
                            //delete contact logic, ask for confirmation!
                            if (parts.length > 1) {
                                parts[1] = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
                                for(Contact c: contactList){
                                    Log.d("CONTACT NAME",c.getName());
                                }
                                Optional<Contact> foundContact = contactList.stream().filter(contact -> contact.getName().equals(parts[1])).findFirst();
                                Log.d("INSIDE DELETE",parts[1]);
                                if (foundContact.isPresent()) {
                                    Log.d("FOUND CONTACT ",foundContact.get().getName());
                                    deleteConfirmation(parts[1]);
                                    isOnDeleteStage = true;
                                    deleteContactName = foundContact.get().getName();
                                }
                            }
                            //display activity for yes/no voice command
                            break;
                        }
                        case "buck":
                        case "back": {
                            finish();
                            break;
                        }
                        case "clear": {
                            filter.setText("");
                            getContacts();
                            break;
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
                        case "call": {
                            if (parts.length > 1) {
                                Intent phone_intent = new Intent(Intent.ACTION_CALL);
                                parts[1] = parts[1].replaceAll("[^a-z0-9]", "");
                                if (parts[1].matches("[0-9]+") && parts[1].length() > 2) {
                                    phone_intent.setData(Uri.parse("tel:" + parts[1]));
                                } else {
                                    Contact contact = findByName(contactList, parts[1]);
                                    phone_intent.setData(Uri.parse("tel:" + contact.getPhone()));
                                }
                                startActivity(phone_intent);
                                break;
                            }
                            break;
                        }
                        case "recent": {
                            if (parts.length > 1) {
                                Contact contact = findByName(contactList, parts[1]);
                                if (contact != null) {
                                    intent = new Intent(Calls.this, History.class);
                                    intent.putExtra("Name", parts[1]);
                                    startActivity(intent);
                                    break;

                                } else {
                                    Toast.makeText(Calls.this, "No contact with given name found!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                getCallLogs();
                                break;
                            }
                        }
                        case "help": {
                            intent = new Intent(Calls.this, Help.class);
                            intent.putExtra("callingActivity", "CallsActivity");
                            startActivity(intent);
                            break;
                        }
                        case "explain": {
                            voiceButton.setEnabled(false);
                            showExplanationDialog();
                            voiceButton.setEnabled(true);
                            break;
                        }
                        case "yes": {
                            if (isOnDeleteStage) {
                                if (deleteContact(deleteContactName)) {
                                    Toast.makeText(Calls.this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
                                    getContacts();
                                    isOnDeleteStage = false;
                                    deleteContactName = "";
                                }
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

    private void deleteConfirmation(String name) {
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(Calls.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(Calls.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "Are you sure you want to delete contact " + name + "?" + "\nPress the button and respond with a yes or no.";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
        textToSpeech.shutdown();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(Calls.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(Calls.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the base of the contacts functionality. Through this activity, by using the correct commands, you can navigate through " +
                            "all of the contacts' functionalities, such as viewing contacts ,editing and creating contacts and call history(or with a contact).\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
        textToSpeech.shutdown();
    }


    private void getContacts() {
        contactList.clear();
        adapter = new ContactAdapter(this, contactList);
        recyclerView.setAdapter(adapter);
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            @SuppressLint("Range") String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            @SuppressLint("Range") String phoneURI = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            Contact contact = new Contact(name, phoneNumber, phoneURI);
            contactList.add(contact);
            Collections.sort(contactList, Comparator.comparing(Contact::getName));
            Log.d("CONTACT SIZE", "Size" + contactList.size());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        speechRecognizer.startListening(intentRecognizer);

    }

    private void filterList(String filter) {
        List<Contact> filteredList = new ArrayList<>();
        for (Contact contact : contactList) {
            if (contact.getName().toLowerCase().contains(filter) || contact.getName().toLowerCase().equals(stripAccents(filter)) || contact.getPhone().contains(filter)
                    || LeveshteinDistance.computeLeveshteinDistance(contact.getName().toLowerCase(), filter) <= 1) {
                filteredList.add(contact);
            }
        }
        contactList.clear();
        contactList.addAll(filteredList);
        Collections.sort(contactList, Comparator.comparing(Contact::getName));
        adapter.notifyDataSetChanged();

    }

    private static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
                } else {

                    Toast.makeText(getApplicationContext(), "Allow Contacts permission is not enabled.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            }
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    break;
                } else {

                    Toast.makeText(getApplicationContext(), "Phone call permission is not enabled.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case 2:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "Phone call permission is not enabled.", Toast.LENGTH_LONG).show();
                    finish();
                }
            case 3:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "Phone call permission is not enabled.", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    private static Contact findByName(Collection<Contact> contactList, String name) {
        return contactList.stream().filter(contact -> name.equals(contact.getName().toLowerCase())).findFirst().orElse(null);
    }

    private void getCallLogs() {
        contactList.clear();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                String callType = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                String callDate = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                String photo = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI));
                Date date = new Date(Long.parseLong(callDate));
                String dir = null;
                if (name != null) {
                    if (!(name.isEmpty())) {
                        int dirCode = Integer.parseInt(callType);
                        switch (dirCode) {
                            case CallLog.Calls.OUTGOING_TYPE:
                                dir = "OUTGOING";
                                break;
                            case CallLog.Calls.INCOMING_TYPE:
                                dir = "INCOMING";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                dir = "MISSED";
                                break;
                        }
                        if (photo != null && photo.isEmpty()) {
                            photo = null;
                        }
                        String builder = date.toString().substring(0, 11) +
                                "- " +
                                dir;
                        Contact contact = new Contact(name, builder, photo);
                        contact.setDate(date.toString());
                        if (!containsContact(contactList, contact.getName())) {
                            contactList.add(contact);
                            Collections.reverse(contactList);
                            // Log.d("CALL INFO:", "NAME: " + name + " DATE " + date + " PHOTO " + photo);
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
            cursor.close();
        }
    }

    private boolean containsContact(List<Contact> list, String name) {
        return list.stream().anyMatch(contact -> contact.getName().equals(name));
    }

    private boolean deleteContact(String name) {
        ArrayList ops = new ArrayList();
        ContentResolver cr = getContentResolver();
        ops.add(ContentProviderOperation
                .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                + " = ?",
                        new String[]{name})
                .build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
            ops.clear();
            return true;

        } catch (OperationApplicationException ex) {
            ex.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String capitalize(String str) {

        if (str == null || str.length() == 0) return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }
}




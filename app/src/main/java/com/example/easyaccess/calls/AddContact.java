package com.example.easyaccess.calls;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.easyaccess.ExplanationDialogHelper;
import com.example.easyaccess.Help;
import com.example.easyaccess.MainActivity;
import com.example.easyaccess.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class AddContact extends AppCompatActivity implements View.OnClickListener {


    private static final int IMAGE_PICK_GALLERY_CODE = 100;
    private SpeechRecognizer speechRecognizer;

    private Intent intentRecognizer;

    private ImageView voiceButton, contactPhoto;

    private String command;

    private EditText contactName, contactNumber;

    private Contact contact;

    private Uri image_Uri;
    private PopupWindow popupWindow;
    private TextView messageTextView;

    private TextToSpeech textToSpeech;
    private AlertDialog alertDialog;
    private TextView dialogTextView;

    private static final String capitalize(String str) {

        if (str == null || str.length() == 0) return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        contactName = findViewById(R.id.contact_name);
        contactNumber = findViewById(R.id.contact_phone);
        contactPhoto = findViewById(R.id.contact_photo);

        String request = getIntent().getStringExtra("Request");
        if (request.equals("edit")) {
            contact = (Contact) getIntent().getSerializableExtra("Contact");
            contactName.setText(contact.getName());
            contactNumber.setText(contact.getPhone());
            if (contact.getPhoto() != null) {
                Picasso.get().load(contact.getPhoto()).into(contactPhoto);

            }
        } else {
            contactName.setText("");
            contactNumber.setText("");
        }


        voiceButton = findViewById(R.id.addContact_image);
        voiceButton.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(AddContact.this, android.Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 0);

        } else {
            Log.d("Contact access permission", "permission is already granted");
            voiceButton.setEnabled(true);
        }

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
                    Log.d("VOICE COMMAND IN CREATE", command);
                    String parts[] = command.split(" ", 3);
                    Intent intent;
                    switch (parts[0]) {
                        case "name": {
                            if (parts.length > 1) {
                                StringBuilder nameBuilder = new StringBuilder();
                                nameBuilder.append(capitalize(parts[1]));
                                if (parts.length > 2) {
                                    nameBuilder.append(" ");
                                    nameBuilder.append(capitalize(parts[2]));
                                }
                                popupWindow.dismiss();
                                contactName.setText(nameBuilder);
                                break;
                            }
                            break;
                        }
                        case "number": {
                            if (parts.length > 1) {
                                Log.d("CALL NUMBER", parts[1]);
                                parts = command.split(" ", 2);
                                parts[1] = parts[1].replaceAll("[^0-9]", "");
                                popupWindow.dismiss();
                                contactNumber.setText(parts[1]);
                                break;
                            }
                            break;
                        }
                        case "image": {
                            popupWindow.dismiss();
                            openGalleryIntent();
                            break;
                        }
                        case "store": {
                            popupWindow.dismiss();
                            if (request.equals("edit")) {
                                saveEdited();
                            } else {
                                saveContact();
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
                            intent = new Intent(AddContact.this, Help.class);
                            intent.putExtra("callingActivity", "AddContactActivity");
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
                    builder.setCancelable(false);

                    // Set the dialog view to a custom layout
                    LayoutInflater inflater = LayoutInflater.from(AddContact.this);
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView);

                    // Get the TextView from the custom layout
                    dialogTextView = dialogView.findViewById(R.id.dialogTextView);

                    // Show the dialog
                    alertDialog = builder.create();
                    alertDialog.show();

                    // Speak the dialog message using TextToSpeech
                    String dialogMessage = "This activity serves the functionality of creating and editing contacts.\nSay 'HELP' to view the available commands!";
                    textToSpeech.speak(dialogMessage, TextToSpeech.QUEUE_FLUSH, null, "dialog_utterance");
                    dialogTextView.setText(dialogMessage);
                }
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceButton.setEnabled(true);
            } else {

                Toast.makeText(getApplicationContext(), "Write Contacts permission is not enabled.", Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    private void saveContact() {
        Log.d("contact number", contactNumber.getText().toString());
        Log.d("contact name", contactName.getText().toString());
        if (!((contactName.getText().toString().equals("")) && contactNumber.getText().toString().equals(""))) {

            ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

            cpo.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            //Adding name
            cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName.getText().toString())
                    .build());

            //Adding number
            cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber.getText().toString())
                    .build());

            //Adding photo if user has selected
            if (image_Uri != null) {
                byte[] imageBytes = imageUriToBytes();

                cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                        .build());
            }


            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
                Toast.makeText(getApplicationContext(), "Saving contact...", Toast.LENGTH_LONG).show();
                finish();
            } catch (OperationApplicationException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try again...", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (RemoteException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try again...", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Please input a number and a name.", Toast.LENGTH_LONG).show();
        }
    }

    private byte[] imageUriToBytes() {
        Bitmap bitmap;
        ByteArrayOutputStream baos = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image_Uri);
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        } catch (Exception e) {
            Log.d("IMAGE", "Image problem");
        }
        return baos.toByteArray();
    }

    private void saveEdited() {
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();
        //add edited number
        cpo.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                                ContactsContract.Data.MIMETYPE + " = ?",
                        new String[]{contact.getName(),
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber.getText().toString())
                .build());
        //add edited name
        cpo.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                                ContactsContract.Data.MIMETYPE + " = ?",
                        new String[]{contact.getName(),
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName.getText().toString())
                .build());

        //add edited photo, if photo exists
        if (image_Uri != null) {
            byte[] imageBytes = imageUriToBytes();
            cpo.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                                    ContactsContract.Data.MIMETYPE + " = ?",
                            new String[]{contact.getName(),
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                    .build());
        }


        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
            Toast.makeText(getApplicationContext(), "Saving contact...", Toast.LENGTH_LONG).show();
            finish();
        } catch (OperationApplicationException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    private void openGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_GALLERY_CODE) {
            image_Uri = data.getData();
            contactPhoto.setImageURI(image_Uri);
        } else {
            Toast.makeText(AddContact.this, "Cancelled photo selection...", Toast.LENGTH_SHORT).show();
        }
    }

}
package com.saveyourride.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saveyourride.R;
import com.saveyourride.utils.Contact;
import com.saveyourride.utils.ContactListAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;


public class SettingsContacts extends AppCompatActivity {

    // DEBUG
    private final String TAG = "SettingsContacts";
    //

    // Permission request code
    private final int READ_CONTACTS_PERMISSIONS_REQUEST_CODE = 1;

    // Pick contact request code
    private final int PICK_CONTACT_REQUEST_CODE = 2;

    // DialogIDs
    private final int READ_CONTACTS_PERMISSION_EXPLANATION_DIALOG = 0;
    private final int READ_CONTACTS_PERMISSION_DENIED_DIALOG = 1;
    private final int ADD_CONTACT_DIALOG = 2;
    private final int EDIT_CONTACT_DIALOG = 3;
    private final int DELETE_CONTACT_DIALOG = 4;
    // Button in Dialog
    Button searchContactButton;
    // Dialog InputFields
    private TextInputLayout firstNameInputLayout, phoneNumberInputLayout;
    private EditText firstNameInput, lastNameInput, phoneNumberInput;
    // SharedPreferences for saved contacts
    private SharedPreferences savedContacts;

    // ArrayList of Contact objects
    private ArrayList<Contact> contactList;

    // Index of the contact being edited
    private int editContactIndex;

    // Index of the contact being deleted
    private int deleteContactIndex;

    // ListAdapter
    private ContactListAdapter listAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_contacts);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        // Set SharedPreferences
        savedContacts = getSharedPreferences(getString(R.string.sp_key_saved_contacts), Context.MODE_PRIVATE);

        // Set contactList
        String contactsJSON = savedContacts.getString(getString(R.string.sp_key_contacts_json), getString(R.string.default_contacts_json));
        Type type = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        contactList = new Gson().fromJson(contactsJSON, type);

        // Set ListView
        ListView contactsListView = (ListView) findViewById(R.id.settingsContacts_contactListView);
        TextView emptyListText = (TextView) findViewById(R.id.settingsContacts_emptyListText);
        contactsListView.setEmptyView(emptyListText);

        // Set up ListAdapter
        listAdapter = new ContactListAdapter(this, contactList);
        contactsListView.setAdapter(listAdapter);
    }

    /**
     * Show a {@code AlertDialog} with the information for a specific notification.
     *
     * @param dialogID determines the information to be shown and the operations to be done.
     */
    private void showAlertDialog(int dialogID) {
        switch (dialogID) {
            case ADD_CONTACT_DIALOG: {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.dialog_emergency_contact, null);

                // InputFields
                firstNameInputLayout = dialogLayout.findViewById(R.id.settingsContacts_dialog_firstNameInputLayout);
                phoneNumberInputLayout = dialogLayout.findViewById(R.id.settingsContacts_dialog_phoneNumberInputLayout);
                firstNameInput = dialogLayout.findViewById(R.id.settingsContacts_dialog_firstNameInput);
                lastNameInput = dialogLayout.findViewById(R.id.settingsContacts_dialog_lastNameInput);
                phoneNumberInput = dialogLayout.findViewById(R.id.settingsContacts_dialog_phoneNumberInput);

                // Button in Dialog
                searchContactButton = dialogLayout.findViewById(R.id.settingsContacts_dialog_searchContactButton);

                // Set onClickListener for searchContactButton
                searchContactButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkContactPermission()) {
                            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST_CODE);
                        }
                    }
                });

                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                // Set the view from XML inside AlertDialog
                alert.setView(dialogLayout);
                alert.setTitle(R.string.title_dialog_add_emergency_contact);
                alert.setPositiveButton(R.string.add, null);
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), R.string.no_contact_added, Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
                final AlertDialog currentDialog = alert.create();
                currentDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (phoneNumberInput.getText().length() == 0) {
                                    phoneNumberInputLayout.setError(getString(R.string.empty_phone_number));
                                } else if (firstNameInput.getText().length() == 0 && lastNameInput.getText().length() == 0) {
                                    firstNameInputLayout.setError(getString(R.string.empty_first_name));
                                } else {
                                    // Add contact to contactList and save it to shared preferences.
                                    Contact contact = new Contact(
                                            firstNameInput.getText().toString(),
                                            lastNameInput.getText().toString(),
                                            phoneNumberInput.getText().toString()
                                    );
                                    contactList.add(contact);
                                    saveContacts(contactList);
                                    listAdapter.notifyDataSetChanged();
                                    currentDialog.cancel();
                                }
                            }
                        });
                    }
                });
                currentDialog.show();
                break;
            }
            case EDIT_CONTACT_DIALOG: {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.dialog_emergency_contact, null);

                // InputFields
                firstNameInputLayout = dialogLayout.findViewById(R.id.settingsContacts_dialog_firstNameInputLayout);
                phoneNumberInputLayout = dialogLayout.findViewById(R.id.settingsContacts_dialog_phoneNumberInputLayout);
                firstNameInput = dialogLayout.findViewById(R.id.settingsContacts_dialog_firstNameInput);
                lastNameInput = dialogLayout.findViewById(R.id.settingsContacts_dialog_lastNameInput);
                phoneNumberInput = dialogLayout.findViewById(R.id.settingsContacts_dialog_phoneNumberInput);

                // Button in Dialog
                searchContactButton = dialogLayout.findViewById(R.id.settingsContacts_dialog_searchContactButton);

                // Set input field
                Contact editContact = contactList.get(editContactIndex);
                setInputFields(editContact.getFirstName(), editContact.getLastName(), editContact.getPhoneNumber());

                // Set onClickListener for searchContactButton
                searchContactButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkContactPermission()) {
                            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST_CODE);
                        }
                    }
                });

                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                // Set the view from XML inside AlertDialog
                alert.setView(dialogLayout);
                alert.setTitle(R.string.title_dialog_edit_emergency_contact);
                alert.setPositiveButton(R.string.edit, null);
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), R.string.no_contact_edited, Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
                final AlertDialog currentDialog = alert.create();
                currentDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (phoneNumberInput.getText().length() == 0) {
                                    phoneNumberInputLayout.setError(getString(R.string.empty_phone_number));
                                } else if (firstNameInput.getText().length() == 0 && lastNameInput.getText().length() == 0) {
                                    firstNameInputLayout.setError(getString(R.string.empty_first_name));
                                } else {
                                    //Update contact in contactList and save changes to shared preferences.
                                    contactList.get(editContactIndex).setContact(
                                            firstNameInput.getText().toString(),
                                            lastNameInput.getText().toString(),
                                            phoneNumberInput.getText().toString()
                                    );
                                    saveContacts(contactList);
                                    listAdapter.notifyDataSetChanged();
                                    currentDialog.cancel();
                                }
                            }
                        });
                    }
                });
                currentDialog.show();
                break;
            }
            case DELETE_CONTACT_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.title_dialog_delete_emergency_contact);

                // Set up the buttons
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contactList.remove(deleteContactIndex);
                        saveContacts(contactList);
                        listAdapter.notifyDataSetChanged();
                    }
                });
                alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            case READ_CONTACTS_PERMISSION_EXPLANATION_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_read_contacts_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_send_sms_permission_explanation);
                // Set up the button
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST_CODE);
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            case READ_CONTACTS_PERMISSION_DENIED_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_read_contacts_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_read_contacts_permission_required);
                // Set up the buttons
                alert.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to app settings
                        Intent appSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        appSettings.setData(uri);
                        startActivity(appSettings);
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH DIALOG!");
                break;
            }
        }
    }

    /**
     * Checks if READ_CONTACTS permission is granted.
     *
     * @return boolean value. If READ_CONTACTS permission is granted: true else false.
     */
    private boolean checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showAlertDialog(READ_CONTACTS_PERMISSION_EXPLANATION_DIALOG);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST_CODE);
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchContactButton.callOnClick();
                } else {
                    showAlertDialog(READ_CONTACTS_PERMISSION_DENIED_DIALOG);
                }
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH REQUEST CODE!");
                break;
            }
        }
    }

    /**
     * This method will be called after user has or has not picked a contact from address book.
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent intent) {
        switch (reqCode) {
            case (PICK_CONTACT_REQUEST_CODE): {
                if (resultCode == RESULT_OK) {
                    Cursor cursor;
                    Uri uri;
                    String[] projection;
                    String selection;

                    // Get CONTACT_ID and HAS_PHONE_NUMBER
                    uri = intent.getData();
                    assert uri != null;
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();
                    String contactID = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String hasPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    cursor.close();

                    // Get first and last name
                    uri = ContactsContract.Data.CONTENT_URI;
                    projection = new String[]{
                            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
                    };
                    selection = ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'" +
                            " AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + contactID;
                    cursor = getContentResolver().query(uri, projection, selection, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();
                    String firstName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                    String lastName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    cursor.close();

                    // Get phone number
                    String phoneNumber = null;
                    if (hasPhoneNumber.equalsIgnoreCase("1")) {
                        uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID;
                        cursor = getContentResolver().query(uri, null, selection, null, null);
                        assert cursor != null;
                        cursor.moveToFirst();
                        phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        cursor.close();
                    }
                    setInputFields(firstName, lastName, phoneNumber);
                } else {
                    // DEBUG
                    Log.d(TAG, "Result code from request: " + resultCode);
                    //
                }
                break;
            }
            default: {
                // DEBUG
                Log.d(TAG, "Request code was: " + reqCode);
                //
            }
        }
    }

    /**
     * Save new or changed {@code contactList} to shared preferences.
     *
     * @param contactList new or changed {@code contactList}
     */
    private void saveContacts(ArrayList<Contact> contactList) {
        // Save new contactList to SharedPreferences
        SharedPreferences.Editor editor = savedContacts.edit();
        String contactsJSON = new Gson().toJson(contactList);
        editor.putString(getString(R.string.sp_key_contacts_json), contactsJSON);
        editor.apply();
    }

    /**
     * Set values in input fields.
     */
    private void setInputFields(String firstName, String lastName, String phoneNumber) {
        firstNameInput.setText(firstName);
        lastNameInput.setText(lastName);
        phoneNumberInput.setText(phoneNumber);
    }

    /**
     * Edit contact data. Called on edit button click.
     *
     * @param position index in {@code contactList} of the contact being edited.
     */
    public void editContact(int position) {
        this.editContactIndex = position;
        showAlertDialog(EDIT_CONTACT_DIALOG);
    }

    /**
     * Delete contact data. Called on delete button click.
     *
     * @param position index in {@code contactList} of the contact being deleted.
     */
    public void deleteContact(int position) {
        this.deleteContactIndex = position;
        showAlertDialog(DELETE_CONTACT_DIALOG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //click listener quasi.
        switch (item.getItemId()) {
            case R.id.settingsContacts_toolbar_addContactButton:
                showAlertDialog(ADD_CONTACT_DIALOG);
                break;
        }
        return super.onOptionsItemSelected(item); // To change body of generated methods, choose Tools | Templates.
    }

    // DEBUG
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
    //
}
package com.saveyourride.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.saveyourride.R;


public class ContactsListAdapter extends ArrayAdapter<Contact> {

    // Debug
    private final String TAG = "ContactsListAdapter";
    ///

    private final Activity context;
    private Contact[] contactsList;

    public ContactsListAdapter(Activity context, Contact[] contactsList) {
        super(context, -1, contactsList);
        this.context = context;
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.contact_list_row_view, parent, false);

        TextView textViewName = (TextView) rowView.findViewById(R.id.contactList_listItem_contactName);
        final String contactFullName = contactsList[position].getFirstName() + " " + contactsList[position].getLastName();
        textViewName.setText(contactFullName);

        Button editButton = (Button) rowView.findViewById(R.id.contactList_listItem_editButton);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO own Activity for edit
                Log.d(TAG, "edit " + contactFullName + ", P-Nr: " + contactsList[position].getPhoneNumber());
            }
        });

        Button deleteBtn = (Button) rowView.findViewById(R.id.contactList_listItem_deleteButton);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Dialog: really delete?
                Log.d(TAG, "delete " + contactFullName + ", P-Nr: " + contactsList[position].getPhoneNumber());
            }
        });
        return rowView;
    }
}
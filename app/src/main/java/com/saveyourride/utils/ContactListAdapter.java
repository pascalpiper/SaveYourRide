package com.saveyourride.utils;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.saveyourride.R;
import com.saveyourride.activities.SettingsContacts;

import java.util.ArrayList;


public class ContactListAdapter extends ArrayAdapter<Contact> {

    // Debug
    private final String TAG = "ContactListAdapter";
    ///

    private SettingsContacts contactsActivity;
    private LayoutInflater mInflater;
    private ArrayList<Contact> contactsList;

    public ContactListAdapter(SettingsContacts context, ArrayList<Contact> contactsList) {
        super(context, -1, contactsList);
        mInflater = context.getLayoutInflater();
        this.contactsActivity = context;
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;

        /*
         * The convertView argument is essentially a "ScrapView".
         * It will have a non-null value when ListView is asking you recycle the row layout.
         * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
         */
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contact_list_row_view, null);

            // set up the ViewHolder
            holder = new ViewHolder();
            holder.textViewName = (TextView) convertView.findViewById(R.id.contactList_listItem_contactName);
            holder.editButton = (Button) convertView.findViewById(R.id.contactList_listItem_editButton);
            holder.deleteButton = (Button) convertView.findViewById(R.id.contactList_listItem_deleteButton);

            // store the holder with the view
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = contactsList.get(position);
        final String contactFullName = contact.getFirstName() + " " + contact.getLastName();

        // Set text in TextView
        holder.textViewName.setText(contactFullName);

        // Set up ButtonsListeners
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DEBUG
                Log.d(TAG, "EDIT: Position: " + position + " Name:" + contactFullName + ", P-Nr: " + contactsList.get(position).getPhoneNumber());
                //
                // TODO check the right way how to implement it better (to this time nothing useful found)
                contactsActivity.editContact(position);

            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DEBUG
                Log.d(TAG, "DELETE: Position: " + position + " Name:" + contactFullName + ", P-Nr: " + contactsList.get(position).getPhoneNumber());
                //
                // TODO also check the right way how to implement it better (to this time nothing useful found)
                contactsActivity.deleteContact(position);
            }
        });
        return convertView;
    }

    /**
     * A ViewHolder object stores each of the component views inside the tag field of the Layout,
     * so you can immediately access them without the need to look them up repeatedly.
     * ViewHolder pattern.
     */
    static class ViewHolder {
        TextView textViewName;
        Button editButton;
        Button deleteButton;
    }
}
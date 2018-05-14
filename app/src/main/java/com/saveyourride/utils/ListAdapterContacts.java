package com.saveyourride.utils;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.saveyourride.R;


public class ListAdapterContacts extends ArrayAdapter {

    // Debug
    private final String TAG = "ListAdapterContacts";
    ///
    private final Activity context;
    private String[] names;
    private String[] phoneNos;

    public ListAdapterContacts(Activity context, String[] names, String[] phoneNos) {
        super(context, R.layout.list_view_content_activity_settings_contacts, names);
        this.context = context;
        this.names = names;
        this.phoneNos = phoneNos;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_view_content_activity_settings_contacts, null, true);

        TextView textViewName = (TextView) rowView.findViewById(R.id.textViewName);
        textViewName.setText(names[position]);

        TextView textViewNumber = (TextView) rowView.findViewById(R.id.textViewNumber);
        textViewNumber.setText(phoneNos[position]);

        ImageButton editBtn = (ImageButton) rowView.findViewById(R.id.imageButtonEdit);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO own Activity for edit
                Log.d(TAG, "edit " + names[position] + " " + phoneNos[position]);
            }
        });

        ImageButton deleteBtn = (ImageButton) rowView.findViewById(R.id.imageButtonDelete);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Dialog: really delete?
                Log.d(TAG, "delete " + names[position] + " " + phoneNos[position]);
            }
        });
        return rowView;
    }
}
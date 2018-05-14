package com.saveyourride.utils;

        import android.app.Activity;
        import android.provider.ContactsContract;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.saveyourride.R;

        import java.sql.SQLOutput;


public class ListAdapterMain extends ArrayAdapter {

    // Debug
    private final String TAG = "ListAdapterContacts";
    ///
    private final Activity context;
    private String[] names;

    public ListAdapterMain(Activity context, String[] names) {
        super(context, R.layout.list_view_content_activity_settings_main, names);
        this.context = context;
        this.names = names;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_view_content_activity_settings_main, null, true);

        TextView textViewName = (TextView)rowView.findViewById(R.id.textViewSettingsMainName);
        textViewName.setText(names[position]);

        return rowView;
    }
}

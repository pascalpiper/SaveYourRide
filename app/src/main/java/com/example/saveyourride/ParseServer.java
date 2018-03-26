package com.example.saveyourride;

import android.content.Context;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/*
 * Created by taraszaika on 02.01.18.
 * new ParseServer
 */
public class ParseServer {

    // Eine (versteckte) Klassenvariable vom Typ der eigene Klasse
    private static ParseServer instance;

    private ParseObject myEventObject;

    // Verhindere die Erzeugung des Objektes über andere Methoden
    // Context appContext is an APP-Context is used for LocalDatastore. At most you use "this"-reference of activity.
    // Without Public Access
    private ParseServer(Context appContext) {

        // Enable Local Datastore.
        Parse.enableLocalDatastore(appContext);

        //ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access
        defaultACL.setPublicReadAccess(true);
        //False = only master key access
        ParseACL.setDefaultACL(defaultACL, true);

        defaultACL.setPublicWriteAccess(true);
        //False = only master key access
        ParseACL.setDefaultACL(defaultACL, true);

        // Only for Logcat and debug mode
        // Parse.addParseNetworkInterceptor(new ParseLogInterceptor());

        //initialize
        final String YOUR_APPLICATION_ID = "MatchFinder";
        final String YOUR_CLIENT_KEY = "matchfinderclientkey";
        final String YOUR_SERVER_URL = "https://matchfinder.dock.moxd.io/api/";
        Parse.initialize(new Parse.Configuration.Builder(appContext)
                .applicationId(YOUR_APPLICATION_ID)
                .clientKey(YOUR_CLIENT_KEY)
                .server(YOUR_SERVER_URL)   // '/' important after 'api'
                .build());

    }

    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.
    // Durch 'synchronized' wird sichergestellt dass diese Methode nur von einem Thread
    // zu einer Zeit durchlaufen wird. Der nächste Thread erhält immer eine komplett
    // initialisierte Instanz.
    public static synchronized ParseServer getInstance(Context appContext) {
        if (ParseServer.instance == null) {
            ParseServer.instance = new ParseServer(appContext);
        }
        return ParseServer.instance;
    }

    public synchronized void deleteEventData(String id) {

        loadEventObjekt(id);

        if (myEventObject != null) {
            myEventObject.deleteInBackground();
        }
        else {
            System.out.println("Keine Objekt gefunden");
        }

    }

    private synchronized void loadEventObjekt(String id) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        try {
            myEventObject = query.get(id);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

package com.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PassiveModeManager extends Service {

    /// ONLY FOR DEBUG
    private static final String TAG = "PassiveModeManager";
    ///

    // Acceleration threshold, when the probability of an accident begins to grow
    private final float MIN_ACCIDENT_ACCELERATION = Accelerometer.ACCIDENT_THRESHOLD;
    // Acceleration, when the probability of an accident is 100%
    private final float MAX_ACCIDENT_ACCELERATION = 230f;
    // Time for which the probability of the accident is stored
    private final long POSSIBLE_ACCIDENT_MILLIS = 30000L; // 30 seconds
    // Minimal time for which the probability of the accident is stored
    private final long MIN_WAIT_TIME = 10000L; // 10 seconds
    // Maximal time for which the probability of the accident is stored
    private final long MAX_WAIT_TIME = 120000L; // 120 seconds or 2 minutes

    // probability of accident. Value in the range of 0.0 to 1.0 where 0.9 is 90%
    private float accidentProbability = 0f;

    // BroadcastReceiver
    private BroadcastReceiver receiver;

    // Intents for Services
    private Intent accelerometerService, locationService;

    // CountDownTimers
    private CountDownTimer possibleAccidentTimer;
    private CountDownTimer noMovementTimer;

    /// ONLY FOR TESTS
    private File dataFileFirst;
    private File dataFileSecond;
    private String dataStringFirst;
    private String dataStringSecond;
    private String currentLocationString;
    ///

    @Override
    public void onCreate() {
        super.onCreate();

        /// ONLY FOR TESTS
        dataFileFirst = getFile(1);
        dataFileSecond = getFile(2);

        dataStringFirst = "LAUNCH: " + getCurrentReadableDate() + "First Part";
        dataStringSecond = "LAUNCH: " + getCurrentReadableDate() + " Second Part";
        currentLocationString = "NO_LOCATION";
        ///

        // Initialize BroadcastReceiver
        initReceiver();

        // Create intents for services
        accelerometerService = new Intent(this.getApplicationContext(), Accelerometer.class);
        locationService = new Intent(this.getApplicationContext(), Location.class);

        // Start Services
        startService(accelerometerService);
        startService(locationService);
    }

    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} and then registers them.
     * {@code receiver} receives the broadcasts from the {@code PassiveMode} activity,
     * {@code Accelerometer} and {@code Location} services.
     */
    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.PASSIVE_MODE_ACTIVITY": {
                        // READ BUTTON PRESSED
                        System.out.println("Accelerometer Shake-Threshold under 100");
                        System.out.println("ACCELEROMETER DATA FILE: \n" + readAccelerometerDataFromFile(dataFileFirst));
                        System.out.println("");
                        System.out.println("-------------------------------------");
                        System.out.println("");
                        System.out.println("Accelerometer Shake-Threshold over 100");
                        System.out.println("ACCELEROMETER DATA FILE: \n" + readAccelerometerDataFromFile(dataFileSecond));
                    }
                    case "android.intent.action.ACCELEROMETER_POSSIBLE_ACCIDENT": {
                        /// ONLY FOR TESTS
                        if (intent.getFloatExtra("acceleration", -1f) < 100f) {
                            dataStringFirst = dataStringFirst + "\n" +
                                    "Acceleration " + intent.getFloatExtra("acceleration", -1f) +
                                    " " + currentLocationString +
                                    " TimeStamp " + getCurrentReadableDate();
                        } else {
                            dataStringSecond = dataStringSecond + "\n" +
                                    "Acceleration " + intent.getFloatExtra("acceleration", -1f) +
                                    " " + currentLocationString +
                                    " TimeStamp " + getCurrentReadableDate();
                        }
                        ///

                        // Before starting the new timer, cancel the old one
                        if (possibleAccidentTimer != null) {
                            possibleAccidentTimer.cancel();
                        }

                        float acceleration = intent.getFloatExtra("acceleration", -1f);
                        float newAccidentProbability = calculateAccidentProbability(MIN_ACCIDENT_ACCELERATION, MAX_ACCIDENT_ACCELERATION, acceleration);
                        accidentProbability = newAccidentProbability > accidentProbability ? newAccidentProbability : accidentProbability;
                        startPossibleAccidentTimer(POSSIBLE_ACCIDENT_MILLIS);
                        break;
                    }
                    case "android.intent.action.ACCELEROMETER_NO_MOVEMENT": {
                        long waitTime = calculateWaitTime(MIN_WAIT_TIME, MAX_WAIT_TIME, accidentProbability);
                        startNoMovementTimer(waitTime);
                        break;
                    }
                    case "android.intent.action.ACCELEROMETER_MOVEMENT_AGAIN": {
                        if (noMovementTimer != null) {
                            noMovementTimer.cancel();
                        }
                        sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));
                        break;
                    }
                    case "android.intent.action.LOCATION": {
                        Log.d(TAG, "Broadcast from Location");
                        Log.d(TAG, "Location Speed: " + intent.getFloatExtra("location_speed", -1f));
                        currentLocationString = "Location" +
                                " Latitude " + intent.getDoubleExtra("location_latitude", -1d) +
                                " Longitude " + intent.getDoubleExtra("location_longitude", -1d) +
                                " Speed " + intent.getFloatExtra("location_speed", -1f);
                        break;
                    }
                    default: {
                        Log.d(TAG, "NO SUCH ACTION IN BROADCAST!");
                        break;
                    }
                }
            }
        };

        // IntentFilter filters messages received by BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.PASSIVE_MODE_ACTIVITY");
        filter.addAction("android.intent.action.ACCELEROMETER_POSSIBLE_ACCIDENT");
        filter.addAction("android.intent.action.ACCELEROMETER_NO_MOVEMENT");
        filter.addAction("android.intent.action.ACCELEROMETER_MOVEMENT_AGAIN");
        filter.addAction("android.intent.action.LOCATION");

        // register our receiver
        registerReceiver(receiver, filter);
    }

    /**
     * Starts the timer which stores the probability of the accident for a certain time
     *
     * @param timerMillis Time for which the value is stored
     */
    private void startPossibleAccidentTimer(long timerMillis) {
        possibleAccidentTimer = new CountDownTimer(timerMillis, timerMillis) {
            @Override
            public void onTick(long millisUntilFinished) {
                // DEBUG
                Log.d(TAG, "POSSIBLE_ACCIDENT_TIMER_ON_TICK: probability of accident is: " + accidentProbability);
                //
            }
            @Override
            public void onFinish() {
                accidentProbability = 0f;
                // DEBUG
                Log.d(TAG, "POSSIBLE_ACCIDENT_TIMER_ON_FINISH: probability of accident is: " + accidentProbability);
                //
            }
        }.start();
    }

    /**
     * Starts the timer. When it expires, the notification will be displayed.
     *
     * @param timerMillis Time to expire before the notification will be displayed.
     */
    private void startNoMovementTimer(long timerMillis) {
        noMovementTimer = new CountDownTimer(timerMillis, timerMillis) {
            @Override
            public void onTick(long millisUntilFinished) {
                // DEBUG
                Log.d(TAG, "NO_MOVEMENT_TIMER_ON_TICK: wait for: " + millisUntilFinished + " milliseconds (in Seconds: " + ((int) millisUntilFinished / 1000) + ")");
                //
            }
            @Override
            public void onFinish() {
                // DEBUG
                Log.d(TAG, "NO_MOVEMENT_TIMER_ON_FINISH: call NotificationSound Service");
                //
                sendBroadcast(new Intent("android.intent.action.NO_MOVEMENT_DETECTED"));
            }
        }.start();
    }

    /**
     * Calculates the probability of accident under consideration of information about the acceleration.
     *
     * @param minAccidentAcceleration Acceleration threshold, when the probability of an accident begins to grow.
     * @param maxAccidentAcceleration Acceleration, when the probability of an accident is 100%
     * @param acceleration            is the acceleration that was noticed recently.
     * @return probability of accident as float value in the range of 0.0 to 1.0 where 0.9 is 90%
     * @throws IllegalArgumentException if {@code minAccidentAcceleration} is greater then {@code maxAccidentAcceleration}.
     * @throws IllegalArgumentException if {@code acceleration} is less then 0 or less then {@code minAccidentAcceleration}.
     */
    private float calculateAccidentProbability(float minAccidentAcceleration, float maxAccidentAcceleration, float acceleration) {
        if (minAccidentAcceleration > maxAccidentAcceleration) {
            throw new IllegalArgumentException("minAccidentAcceleration is greater then maxAccidentAcceleration");
        }
        if (acceleration < 0 || acceleration < minAccidentAcceleration) {
            throw new IllegalArgumentException("acceleration is less then 0 or less then minAccidentAcceleration");
        }
        if (acceleration >= maxAccidentAcceleration) {
            return 1f;
        } else {
            float differenceAcceleration = maxAccidentAcceleration - minAccidentAcceleration;
            float probability = 1f - ((maxAccidentAcceleration - acceleration) / differenceAcceleration);
            return probability >= 1f ? 1f : probability;
        }
    }

    /**
     * Calculates the time that the application should wait before sending the notification to the user
     * based on the minimum and maximum waiting time and probability of an accident.
     *
     * @param minWaitTime minimal time to wait before sending the notification.
     * @param maxWaitTime maximal time to wait before sending the notification.
     * @param probability probability of an accident.
     * @return the time that the application should wait before sending the notification to the user.
     * @throws IllegalArgumentException if {@code minWaitTime} is greater then {@code maxWaitTime}.
     * @throws IllegalArgumentException if {@code probability} is less then 0.
     */
    private long calculateWaitTime(long minWaitTime, long maxWaitTime,
                                   float probability) {
        if (minWaitTime > maxWaitTime) {
            throw new IllegalArgumentException("minWaitTime is greater then maxWaiTime");
        }
        if (probability < 0) {
            throw new IllegalArgumentException("probability is less then 0");
        }
        if (probability == 0f) {
            return maxWaitTime;
        } else if (probability >= 1f) {
            return minWaitTime;
        } else {
            float differenceTime = maxWaitTime - minWaitTime;
            return minWaitTime + (long) (differenceTime * (1f - probability));
            // (1-probability) because the greater the probability, the less you want to wait
        }
    }

    /// ONLY FOR TESTS
    private File getFile(int part) {
        File path = getApplicationContext().getFilesDir();
        if (part == 1) {
            return new File(path, "accelerometer_data.txt");
        } else {
            return new File(path, "accelerometer_data_2.txt");
        }
    }

    private void writeAccelerometerDataToFile(File file, String data) {
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readAccelerometerDataFromFile(File file) {
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }

    private String getCurrentReadableDate() {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date resultDate = new Date(currentTimeMillis);
        return sdf.format(resultDate);
    }
    ///

    /**
     * Will be called after stopService(controlServiceIntent).
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // DEBUG
        System.out.println("ON DESTROY " + TAG);
        //

        if (possibleAccidentTimer != null) {
            possibleAccidentTimer.cancel();
        }
        if (noMovementTimer != null) {
            noMovementTimer.cancel();
        }
        unregisterReceiver(receiver);

        stopService(accelerometerService);
        stopService(locationService);

        /// ONLY FOR TESTS Write received broadcast into file
        writeAccelerometerDataToFile(dataFileFirst, dataStringFirst);
        writeAccelerometerDataToFile(dataFileSecond, dataStringSecond);
        ///
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

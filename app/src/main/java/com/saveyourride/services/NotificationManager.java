package com.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class NotificationManager extends Service {

    // DEBUG
    private final String TAG = "NotificationManager";
    //
    /// Time of Notifications
    // ITM
    private final long ITM_NOTIFICATION_SOUND_TIME = 5500L;
    private final long ITM_NOTIFICATION_DIALOG_TIME = 20000L;
    // AGP
    private final long AGP_NOTIFICATION_TIME = 45000L; // 20000L;
    // NMD
    private final long NMD_NOTIFICATION_SOUND_TIME = 5000L;  // 5500L;
    private final long NMD_NOTIFICATION_DIALOG_TIME = 45000L; // 20000L;

    private boolean isWaiting;
    private boolean isPlaying;
    private int waitCounter;

    private BroadcastReceiver receiver;
    private MediaPlayer mMediaPlayer;
    private AudioManager audioManager;
    private CountDownTimer currentTimer;
    private int currentAudioVolume;


    @Override
    public void onCreate() {
        super.onCreate();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initReceiver();
    }


    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} for messages from {@code ActiveModeManager} and {@code ActiveMode}
     * and registers them.
     * {@code receiver} receives the broadcasts from the ActiveModeManager and ActiveMode activity.
     */
    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.STOP_NOTIFICATION": {
                        if (currentTimer != null) {
                            currentTimer.cancel();
                        }
                        mMediaPlayer.stop();
                        break;
                    }


                    case "android.intent.action.ACCIDENT_GUARANTEE_PROCEDURE": {
                        notificationNMD();
                        break;
                    }

                    case "android.intent.action.INTERVAL_TIME_EXPIRED": {
                        notificationITM();
                        break;
                    }
                    case "android.intent.action.NO_MOVEMENT_DETECTED": {
                        notificationNMD();
                        break;
                    }
                    default:
                        Log.d(TAG, "NO ACTION IN BROADCAST!");
                        break;
                }

            }
        };

        IntentFilter notificationFilter = new IntentFilter();

        notificationFilter.addAction("android.intent.action.STOP_NOTIFICATION");
        notificationFilter.addAction("android.intent.action.ACCIDENT_GUARANTEE_PROCEDURE");

        notificationFilter.addAction("android.intent.action.INTERVAL_TIME_EXPIRED");
        notificationFilter.addAction("android.intent.action.NO_MOVEMENT_DETECTED");


        registerReceiver(receiver, notificationFilter);
    }

    /**
     * Control notification when the Interval Time from an interval from {@code ActiveModeManager}
     * is expired.
     * Start the timer which start/stop the sound and the dialog of the notification
     * ITM = INTERVAL_TIME_EXPIRED
     */
    public void notificationITM() {

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/notification_sound");

        try {
            startSound(sound);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendBroadcast(new Intent("android.intent.action.ITM_SHOW_DIALOG"));

        currentTimer = new CountDownTimer(ITM_NOTIFICATION_DIALOG_TIME, ITM_NOTIFICATION_SOUND_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished <= ITM_NOTIFICATION_DIALOG_TIME - ITM_NOTIFICATION_SOUND_TIME && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAudioVolume, AudioManager.FLAG_SHOW_UI);
                }
            }
            @Override
            public void onFinish() {
                sendBroadcast(new Intent("android.intent.action.DISMISS_DIALOG"));
            }
        }.start();
    }

    /**
     * Control notification when the {@code PassiveModeManager} detects no movement.
     * NMD = NO_MOVEMENT_DETECTED
     */
    public void notificationNMD() {

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/notification_sound");

        isWaiting = false;
        isPlaying = false;
        waitCounter = 0;


        sendBroadcast(new Intent("android.intent.action.NMD_SHOW_DIALOG"));

        currentTimer = new CountDownTimer(NMD_NOTIFICATION_DIALOG_TIME, NMD_NOTIFICATION_SOUND_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {

                if (!isWaiting && !isPlaying) {
                    try {
                        // TODO find better solution for Sound URI
                        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/notification_sound");
                        startSound(sound);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isPlaying = true;
                } else if (!isWaiting && isPlaying) {
                    mMediaPlayer.stop();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAudioVolume, AudioManager.FLAG_SHOW_UI);
                    isWaiting = true;
                    isPlaying = false;
                } else if (isWaiting && waitCounter == 2) {
                    isWaiting = false;
                    waitCounter = 0;
                } else {
                    waitCounter++;
                }
            }


            @Override
            public void onFinish() {
                sendBroadcast(new Intent("android.intent.action.DISMISS_DIALOG"));
                mMediaPlayer.stop();
                notificationAGP();

            }
        }.start();
    }


    /**
     * Control notification for the accident guarantee procedure from {@code NotificationManager}
     * is expired.
     * Start the timer which start/stop the sound and the dialog of the notification
     * AGP = ACCIDENT_GUARANTEE_PROCEDURE
     */
    public void notificationAGP() {

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/alarm_sound");

        try {
            startSound(sound);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendBroadcast(new Intent("android.intent.action.AGP_SHOW_DIALOG"));

        currentTimer = new CountDownTimer(AGP_NOTIFICATION_TIME, AGP_NOTIFICATION_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mMediaPlayer.stop();
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAudioVolume, AudioManager.FLAG_SHOW_UI);

                sendBroadcast(new Intent("android.intent.action.DISMISS_DIALOG"));

                // TODO CALL SOS_MODE
                // DEBUG
                Log.d(TAG, "Call SOS MODE");

            }
        }.start();
    }

    public void startSound(Uri sound) throws IOException {

//      Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(getApplicationContext(), sound);

        currentAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        if (currentAudioVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_SHOW_UI);
        }
        // TODO change streamType
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // For Testing
//        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        currentTimer.cancel();
        mMediaPlayer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

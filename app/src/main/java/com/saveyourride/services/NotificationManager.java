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

    private BroadcastReceiver receiver;

    private MediaPlayer mMediaPlayer;
    private AudioManager audioManager;
    private CountDownTimer currentTimer;

    private int currentAudioVolume;

    private final long ITM_NOTIFICATION_SOUND_TIME = 2700L;  // 5500L;
    private final long ITM_NOTIFICATION_DIALOG_TIME = 5000L; // 20000L;


    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Start NotificationSound");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action) {
                    case "android.intent.action.INTERVAL_TIME_EXPIRED": {

                        notificationITM();

                        break;
                    }

                    case "android.intent.action.ACCIDENT_GUARANTEE_PROCEDURE": {

                        long notificationTime = intent.getLongExtra("notificationSoundTime", -1);
                        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/notification_sound_service_alarm_signal");


                        notificationITM();

                        break;
                    }

                    default:
                        Log.d(TAG, "NO ACTION IN BROADCAST!");
                }

            }
        };

        IntentFilter notificationFilter = new IntentFilter();

        notificationFilter.addAction("android.intent.action.INTERVAL_TIME_EXPIRED");
        notificationFilter.addAction("android.intent.action.ACCIDENT_GUARANTEE_PROCEDURE");


        registerReceiver(receiver, notificationFilter);
    }

    /**
     * Control notification when the Interval Time from an interval from {@code ActiveModeManager}
     * is expired.
     * Start the timer which start/stop the sound and the dialog of the notification
     * ITM = INTERVAL_TIME_EXPIRED
     */
    public void notificationITM() {

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/notification_sound_service_alarm_signal");
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

    public void startSound(Uri sound) throws IOException {

//      Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(getApplicationContext(), sound);

        currentAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        if (currentAudioVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_SHOW_UI);
        }

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

    private void stopSound() {
        mMediaPlayer.stop();
        if (currentTimer != null) {
//            currentTimer.onFinish();
            currentTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

        stopSound();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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

import java.io.IOException;

public class NotificationSound extends Service {


    private BroadcastReceiver startNotificationReceiver;
    private BroadcastReceiver stopNotificationReceiver;

    private MediaPlayer mMediaPlayer;
    private AudioManager audioManager;
    private CountDownTimer currentTimer;

    private int currentAudioVolume;


    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Start NotificationSound");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        startNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("start sound");
                long notificationTime = intent.getLongExtra("notificationSoundTime", -1);
                startNotification(notificationTime);
            }
        };

        stopNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("stop sound");
                stopSound();
            }
        };

        IntentFilter startNotificationFilter = new IntentFilter("android.intent.action.START_NOTIFICATION");
        IntentFilter stopNotificationFilter = new IntentFilter("android.intent.action.STOP_NOTIFICATION");

        registerReceiver(startNotificationReceiver, startNotificationFilter);
        registerReceiver(stopNotificationReceiver, stopNotificationFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void startNotification(long notificationSoundTime) {

        try {
            startSound();
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentTimer = new CountDownTimer(notificationSoundTime, notificationSoundTime) {
            @Override
            public void onTick(long millisUntilFinished) {
                System.out.println("OnTick" + millisUntilFinished);

            }

            @Override
            public void onFinish() {
                System.out.println("ON FINISH FROM NOTIFICATION PLAYER");
                mMediaPlayer.stop();
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAudioVolume, AudioManager.FLAG_SHOW_UI);

//                mMediaPlayer.release();

            }
        }.start();
    }

    public void startSound() throws IOException {
//        try {
//            Uri path = Uri.parse("android.resource://"+getPackageName()+"/raw/sound.mp3");
//            // The line below will set it as a default ring tone replace
//            // RingtoneManager.TYPE_RINGTONE with RingtoneManager.TYPE_NOTIFICATION
//            // to set it as a notification tone
//            RingtoneManager.setActualDefaultRingtoneUri(
//                    getApplicationContext(), RingtoneManager.TYPE_RINGTONE,
//                    path);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), path);
//            r.play();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri alert = Uri.parse("android.resource://" + this.getPackageName() + "/raw/notification_sound_service_alarm_signal");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(getApplicationContext(), alert);

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
        unregisterReceiver(startNotificationReceiver);
        unregisterReceiver(stopNotificationReceiver);

        stopSound();

    }
}

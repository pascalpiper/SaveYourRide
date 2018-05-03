package com.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;

public class Notification extends Service {

    private BroadcastReceiver saveIfAccident;
    private BroadcastReceiver aktiveModeInterval;
    private BroadcastReceiver passiveMode;

    private IntentFilter saveIfAccidentFilter;
    private IntentFilter aktiveModeIntervalFilter;
    private IntentFilter passiveModeFilter;
    private MediaPlayer mMediaPlayer;

    private AudioManager audioManager;


    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Start Notification");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        saveIfAccident = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        aktiveModeInterval = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("sound?");
                try {
                    notifySound();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        passiveMode = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        aktiveModeIntervalFilter = new IntentFilter("android.intent.action.ACTIVE_MODE_INTERVAL");
        registerReceiver(aktiveModeInterval, aktiveModeIntervalFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void notifySound() throws IOException {
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

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(this, alert);

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(audioManager.STREAM_ALARM), audioManager.FLAG_SHOW_UI);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(aktiveModeInterval);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, audioManager.FLAG_SHOW_UI);
        mMediaPlayer.stop();
    }
}

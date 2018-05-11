package com.saveyourride.activities;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.saveyourride.R;


    public class Activity_Active extends Activity implements View.OnClickListener {
        /** Called when the activity is first created. */
        AnimationDrawable mFrameAnimation = null;

        boolean mbUpdating = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity__active);

            Button btnWheel = (Button)findViewById(R.id.wheel_button);
            btnWheel.setOnClickListener(this);

            mFrameAnimation = (AnimationDrawable) btnWheel.getBackground();
        }

        public void onClick(View v) {
            if(v.getId() == R.id.wheel_button) {
                if(!mbUpdating) {
                    mbUpdating = true;
                    new AsyncTaskForUpdateDB().execute("");
                }
            }

        }

        private class AsyncTaskForUpdateDB extends AsyncTask<String, Integer, ResultOfAsyncTask> {

            @Override
            protected void onPreExecute() {

                mFrameAnimation.start();
                super.onPreExecute();
            }

            @Override
            protected ResultOfAsyncTask doInBackground(String... strData) {
                ResultOfAsyncTask result = new ResultOfAsyncTask();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(ResultOfAsyncTask result) {
                mFrameAnimation.stop();
                mbUpdating = false;
            }

            @Override
            protected void onCancelled() {
                mFrameAnimation.stop();
                mbUpdating = false;
                super.onCancelled();
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
            }
        }

        private class ResultOfAsyncTask {
            int iErrorCode = 0;
        }
    }
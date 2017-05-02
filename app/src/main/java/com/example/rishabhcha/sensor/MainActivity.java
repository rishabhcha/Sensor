package com.example.rishabhcha.sensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.DEFAULT_LIGHTS;
import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;


public class MainActivity extends AppCompatActivity {

    private TextView tempTextView, gasTextView, fireTextView;
    private Button button;
    private Switch switch1;
    private boolean isWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTextView = (TextView) findViewById(R.id.tempTextView);
        fireTextView = (TextView) findViewById(R.id.fireTextView);
        gasTextView = (TextView) findViewById(R.id.gasTextView);

        button= (Button)findViewById(R.id.button);
        switch1 = (Switch) findViewById(R.id.switch1);

        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                //getPicture();
                new RetrieveFeedTask().execute();

            }
        }, 0, 10000);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RetrieveFeedTask().execute();

            }
        });


        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    isWater = true;
                    new RetrieveFeedTaskRelay().execute();


                }else {

                    isWater = false;
                    new RetrieveFeedTaskRelay().execute();

                }

            }
        });

    }


    class RetrieveFeedTask extends AsyncTask<Object, Object, String> {

        private Exception exception;

        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Object... urls) {

            try {
                URL url = new URL("http://peastech.in/kitchen.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    urlConnection.disconnect();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        protected void onPostExecute(String response) {

            //progressBar.setVisibility(View.GONE);
            Log.i("------------INFO------", response);

            response = response.trim();

            String[] ar = response.split(",");
            String t = ar[0];
            String f = ar[1];
            String g = ar[2];

            tempTextView.setText(t);
            fireTextView.setText(f);
            gasTextView.setText(g);

            if (Double.parseDouble(t) > 60){

                create_notification("Temperature above threshold!");


            }

            if (Double.parseDouble(g) > 400){

                create_notification("Gas above threshold!");

            }

            if (Double.parseDouble(f) > 800){

                create_notification("Fire Alert!");
                switch1.setChecked(true);

            }

        }
    }


    class RetrieveFeedTaskRelay extends AsyncTask<Object, Object, String> {

        private Exception exception;

        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Object... urls) {

            try {

                URL url;

                if (isWater){

                    url = new URL("http://peastech.in/kitchen.php?relay=1");
                }
                else {
                    url = new URL("http://peastech.in/kitchen.php?relay=0");
                }

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    urlConnection.disconnect();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        protected void onPostExecute(String response) {

            if (isWater){

                Log.d("----sending sms","  yes");
                sendSms("+919629775546","Fire Alert!");

            }

        }
    }

    private void sendSms(String s, String s1) {

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(s,null,s1,null,null);

    }

    private void create_notification(String msg){

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Sensor")
                .setContentText(msg)
                .setAutoCancel(true)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)
                System.currentTimeMillis(), intent, 0);
        mBuilder.setContentIntent(pendingIntent);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

    }

}




package com.plk.smartq.smartalert;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class WaitActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "SmartAlert";
    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://broker.hivemq.com:1883";
    final String clientId = "smartqplk";
    MediaPlayer mPlayer;

    TextView txt_wait;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
        pref = getSharedPreferences(PREFS_NAME, 0);

        String hoscode = pref.getString("hoscode", "");
        String number = pref.getString("number", "");

        txt_wait = (TextView) findViewById(R.id.txt_wait);

        Intent intent = getIntent();


        if (intent.hasExtra("msg")) {
            Bundle extras = intent.getExtras();
            String msg = extras.getString("msg", "1,1");
            String[] a = msg.split(",");
            String info = "หมายเลข " + a[0] + " อีก 5 คิวจะถึงคิวของท่าน กรุณาไปรอที่บริเวณ " + a[1];
            txt_wait.setText(info);

        } else {
            txt_wait.setText("หมายเลข " + number + " กรุณารอคิวสักครู่...");
        }


        String mTopic = "smartq/" + hoscode;
        setupMqttClient(mTopic);


    }

    @Override
    protected void onDestroy() {
        mPlayer.stop();
        super.onDestroy();
    }

    private void setupMqttClient(final String sTopic) {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    //addToHistory("[CON] Reconnected to : " + serverURI);
                } else {
                    //addToHistory("[CON] Connected to: " + serverURI);
                }
                subscribeToTopic(sTopic);
            }

            @Override
            public void connectionLost(Throwable cause) {
                //addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //addToHistory("Incoming message: " + new String(message.getPayload()));
                String msg_alert = new String(message.getPayload());
                Log.d("smart_alert", msg_alert);
                String[] b = msg_alert.split(",");
                Log.d("smart_alert_split", b[0]);
                String _number = pref.getString("number", "");
                if (b[0].equals(_number)) {
                    vibrate();
                    addNotification(msg_alert);
                    mPlayer.start();

                    String info = "หมายเลข " + b[0] + " อีก 5 คิวจะถึงคิวของท่าน กรุณาไปรอที่บริเวณ " + b[1];
                    txt_wait.setText(info);
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);


        try {
            mqttAndroidClient.connect(mqttConnectOptions, null);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void subscribeToTopic(String s_topic) {
        try {
            //mqttAndroidClient.unregisterResources();
            mqttAndroidClient.subscribe(s_topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //addToHistory("Subscribed!");
                    Log.d("smart_alert", "Subscribed! Success..");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to subscribe");
                    Log.d("smart_alert", "Subscribed! Fail..");
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    private void vibrate() {

        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vb.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vb.vibrate(500);
        }

    }

    private void addNotification(String qAlert) {

        Context context = getApplicationContext();
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        Intent intent = new Intent(context, WaitActivity.class);
        intent.putExtra("msg", qAlert);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification =
                new NotificationCompat.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(bitmap)
                        .setContentTitle("แจ้งเตือนใกล้ถึงคิว")
                        .setContentText(qAlert)
                        //.setChannel("smart")
                        .setAutoCancel(true)
                        .setColor(color)
                        .build();


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1000, notification);


    }


}

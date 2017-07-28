package com.fourall.pushgeofencing.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import com.fourall.pushgeofencing.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class PushMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    private static final String NOTIFICATION_BODY = "messageBody";
    private static final String NOTIFICATION_TITLE = "messageTitle";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            Map<String, String> map = remoteMessage.getData();
            setNotificationDefault(map.get(NOTIFICATION_TITLE), map.get(NOTIFICATION_BODY));
        }else{
            super.onMessageReceived(remoteMessage);
        }
    }

    private void setNotificationDefault(String title, String body) {
        //por enquanto em distinção de background ou foreground
        Notification.Builder builder =
                new Notification.Builder(this)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentText(body);
        //Vibration
        builder.setVibrate(new long[]{500, 500, 500, 500, 1000});
        //LED
        builder.setLights(Color.RED, 3000, 3000);
        //Ton
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notification);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(1, builder.build());
    }
}
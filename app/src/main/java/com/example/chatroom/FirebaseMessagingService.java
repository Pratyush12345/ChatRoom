package com.example.chatroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    NotificationCompat.Builder notification;
    private String channel_id="personal notification";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_body=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");


        notification=new NotificationCompat.Builder(this,channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_body);


        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);

        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(pendingIntent);

        int notifivation_id=(int)System.currentTimeMillis();

        NotificationManager mNotifyMan=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMan.notify(notifivation_id,notification.build());



    }
}

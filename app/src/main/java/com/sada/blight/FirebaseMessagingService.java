package com.sada.blight;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Shaan on 28-11-2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationMessage = remoteMessage.getNotification().getBody();
//        String clickAction = remoteMessage.getNotification().getClickAction();
//        String fromUserId = remoteMessage.getData().get("fromUserId");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationMessage);

        Intent resultIntent = new Intent(getApplicationContext(), HomeActivity.class);
        resultIntent.putExtra("showAlert", true);
        resultIntent.putExtra("title", notificationTitle);
        resultIntent.putExtra("message", notificationMessage);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        } else {
            Toast.makeText(getApplicationContext(), "NULL POINTER", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.thunderbolt_108.shipperapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.thunderbolt_108.shipperapp.Common.Common;
import com.example.thunderbolt_108.shipperapp.Helper.NotificationHelper;
import com.example.thunderbolt_108.shipperapp.HomeActivity;
import com.example.thunderbolt_108.shipperapp.MainActivity;
import com.example.thunderbolt_108.shipperapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() !=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationAPI26(remoteMessage);
            else
                sendNotification(remoteMessage);
        }
    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String,String> data=remoteMessage.getData();
        String title=data.get("title");
        String message=data.get("message");

        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if (Common.currentShipper !=null) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getNotificationChannel(title, message, pendingIntent, defaultSoundUri);

            //Get random id for notification to show all notification
            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
        else
        {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.getNotificationChannel(title, message, defaultSoundUri);
            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {

            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String message = data.get("message");

        if (Common.currentShipper !=null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.food)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.mipmap.food))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            noti.notify(0, builder.build());
        }
        else
        {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.food)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.mipmap.food))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);
            NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            noti.notify(0, builder.build());
        }
    }
}


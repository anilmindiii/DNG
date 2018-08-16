package com.dng.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dng.R;
import com.dng.activity.MainActivity;
import com.dng.helper.Constant;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    String userId, reqId;
    String title = "";
    String message = "";
    String deliveryId = "";
    String orderId = "";
    String uid;
    String fcmToken;
    String username;
    String type;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            title = remoteMessage.getData().get("title");
            type = remoteMessage.getData().get("type");
            message = remoteMessage.getData().get("message");
            orderId = remoteMessage.getData().get("orderId");
            deliveryId = remoteMessage.getData().get("deliveryId");




            Intent intent = new Intent("com.dng");
            intent.putExtra("type", type);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.putExtra("orderId", orderId);
            intent.putExtra("deliveryId", deliveryId);
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.sendBroadcast(intent);


            sendNotification(type, title, message, orderId, deliveryId);

        }

    }

    private void sendNotification(String type, String title, String message, String orderId, String deliveryId) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("orderId", orderId);
        intent.putExtra("deliveryId", deliveryId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(this.title)
                .setContentText(this.message)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);

        }
        notificationManager.notify(5, notificationBuilder.build());
    }
}





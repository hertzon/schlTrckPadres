package com.coltrack.schooltrackpadres;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver{
    String TAG="Login";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //-----------------------------
        // Create a test notification
        //
        // (Use deprecated notification
        // API for demonstration purposes,
        // to avoid having to import
        // the Android Support Library)
        //-----------------------------

        String notificationTitle = "Pushy";
        String notificationDesc = "Test notification";

        //-----------------------------
        // Attempt to grab the message
        // property from the payload
        //
        // We will be sending the following
        // test push notification:
        //
        // {"message":"Hello World!"}
        //-----------------------------

        if ( intent.getStringExtra("message") != null )
        {
            notificationDesc = intent.getStringExtra("message");
            Log.i(TAG,"Llego mensaje: "+notificationDesc);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});


        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND);

        mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
        mBuilder.setContentTitle("Titulto");
        mBuilder.setContentText("Texto Contenido");
        mBuilder.setTicker("ticker");

        Intent inotificacion=new Intent(context,Login.class);
        PendingIntent intentpendiente=PendingIntent.getActivity(context,0,inotificacion,0);
        mBuilder.setContentIntent(intentpendiente);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(10,mBuilder.build());






//        //-----------------------------
//        // Create a test notification
//        //-----------------------------
//
//        Notification notification = new Notification(android.R.drawable.ic_dialog_info, notificationDesc, System.currentTimeMillis());
//
//        //-----------------------------
//        // Sound + vibrate + light
//        //-----------------------------
//
//        notification.defaults = Notification.DEFAULT_ALL;
//
//        //-----------------------------
//        // Dismisses when pressed
//        //-----------------------------
//
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//
//        //-----------------------------
//        // Create pending intent
//        // without a real intent
//        //-----------------------------
//
//        notification.setLatestEventInfo(context, notificationTitle, notificationDesc, null);
//
//        //-----------------------------
//        // Get notification manager
//        //-----------------------------
//
//        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        //-----------------------------
//        // Issue the notification
//        //-----------------------------
//
//        mNotificationManager.notify(0, notification);
    }
}
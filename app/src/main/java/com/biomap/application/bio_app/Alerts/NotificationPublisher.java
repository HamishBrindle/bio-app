package com.biomap.application.bio_app.Alerts;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.R;

/**
 * Created by Vafa Dehghan Saei for BioMapon 2017-06-27.
 */

public class NotificationPublisher extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent homeIntent = new Intent(context, MainActivity.class);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_logo_svg)
                .setColor(Color.parseColor("#4c4c4c"))
                .setContentTitle("It's time to move!")
                .setContentText("You should get up and offset the pressure")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(Color.RED, 1000, 250)
                .setAutoCancel(true);


        PendingIntent resultHomeItent = PendingIntent.getActivity(
                context,
                0,
                homeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultHomeItent);
        notificationManager.notify(1, mBuilder.build());
    }
}

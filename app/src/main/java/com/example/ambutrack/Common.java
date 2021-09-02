package com.example.ambutrack;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.ambutrack.ui.AnimationModel;
import com.example.ambutrack.ui.DriverGeoModel;
import com.example.ambutrack.ui.DriverInfoModel;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

public class Common {
    public static final String DRIVERS_LOCATION_REFERENCES = "DriversLocation";
    public static final String DRIVERS_INFO_REFERENCES = "DriverInfo";
    public static final String TOKEN_REFERENCE = "Token";
    public static final String REQUEST_DRIVER_TITLE = "RequestDriver";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "body";
    public static final String RIDER_PICKUP_LOCATION = "PickupLocation";
    public static final String RIDER_KEY = "RiderKey";
    public static final String REQUEST_DRIVER_DECLINE = "Decline";
    public static final String DRIVER_KEY = "DriverKey";
    public static final String USER_INFO_REFERENCE = "User";
    public static final String RIDER_PICKUP_LOCATION_STRING = "PickupLocationString";
    public static int FLAG = 0;
    public static DriverInfoModel currentUser;
    public static User currentPatient;

    public static Map<String,DriverGeoModel> driversFound = new HashMap<>();
    public static HashMap<String, Marker> markerList = new HashMap<>();
    public static HashMap<String, AnimationModel> driverLocationSubscribe = new HashMap<String, AnimationModel>();

    public static String buildName(String name) {
        return name;
    }


    public static ValueAnimator valueAnimate(long duration, ValueAnimator.AnimatorUpdateListener listener){
        ValueAnimator va = ValueAnimator.ofFloat(0,100);
        va.setDuration(duration);
        va.addUpdateListener(listener);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);
        va.start();
        return va;
    }

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null){
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "com_example_ambutrack";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Ambutrack",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Ambutrack");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_directions_car_24));
        if(pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id,notification);
    }

}

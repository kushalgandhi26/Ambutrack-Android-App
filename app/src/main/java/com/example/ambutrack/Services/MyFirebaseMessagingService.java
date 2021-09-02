package com.example.ambutrack.Services;

import androidx.annotation.NonNull;

import com.example.ambutrack.Common;
import com.example.ambutrack.EventBus.DeclineRequestFromDriver;
import com.example.ambutrack.EventBus.DriverRequestReceived;
import com.example.ambutrack.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            UserUtils.updateToken(this,s);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> dataRecv = remoteMessage.getData();
        if(dataRecv != null){
            if(dataRecv.get(Common.NOTI_TITLE) != null){
                if(dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_DECLINE)){
                    EventBus.getDefault().postSticky(new DeclineRequestFromDriver());
                }
            }
            if(dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_TITLE)){
                EventBus.getDefault().postSticky(new DriverRequestReceived(dataRecv.get(Common.RIDER_KEY),
                        dataRecv.get(Common.RIDER_PICKUP_LOCATION)));
            }else {
                Common.showNotification(this,new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }
        }
    }
}

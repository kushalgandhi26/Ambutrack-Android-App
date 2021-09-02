package com.example.ambutrack.Utils;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ambutrack.Common;
import com.example.ambutrack.R;
import com.example.ambutrack.Remote.IFCMService;
import com.example.ambutrack.Remote.RetrofitFCMClient;
import com.example.ambutrack.ui.DriverGeoModel;
import com.example.ambutrack.ui.FCMSendData;
import com.example.ambutrack.ui.TokenModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UserUtils {
    public static void sendRequestToDriver(Context context, RelativeLayout root_layout, DriverGeoModel foundDriver, LatLng destination) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(foundDriver.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                            Map<String,String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_TITLE);
                            notificationData.put(Common.NOTI_CONTENT,"This message for request driver action");
                            notificationData.put(Common.RIDER_KEY,FirebaseAuth.getInstance().getCurrentUser().getUid());
                            notificationData.put(Common.RIDER_PICKUP_LOCATION,new StringBuilder("")
                            .append(destination.latitude)
                            .append(",")
                            .append(destination.longitude)
                            .toString());

                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(),notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(fcmResponse -> {
                                if(fcmResponse.getSuccess() ==0){
                                    compositeDisposable.clear();
                                    Snackbar.make(root_layout,context.getString(R.string.requesr_driver_failed),Snackbar.LENGTH_LONG).show();
                                }
                            }, throwable -> {
                                compositeDisposable.clear();
                                Snackbar.make(root_layout,throwable.getMessage(),Snackbar.LENGTH_LONG).show();
                            }));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(root_layout,error.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public static void updateToken(Context context, String s) {
        TokenModel tokenModel = new TokenModel(s);
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    public static void sendDeclineRequest(View view, Context context, String key) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                            Map<String,String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_DECLINE);
                            notificationData.put(Common.NOTI_CONTENT,"This message for action driver decline");
                            notificationData.put(Common.DRIVER_KEY,FirebaseAuth.getInstance().getCurrentUser().getUid());

                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(),notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if(fcmResponse.getSuccess() ==0){
                                            compositeDisposable.clear();
                                            Snackbar.make(view,context.getString(R.string.decline_failed),Snackbar.LENGTH_LONG).show();
                                        }else {
                                            Snackbar.make(view,context.getString(R.string.decline_success),Snackbar.LENGTH_LONG).show();
                                        }
                                    }, throwable -> {
                                        compositeDisposable.clear();
                                        Snackbar.make(view,throwable.getMessage(),Snackbar.LENGTH_LONG).show();
                                    }));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(view,error.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public static void updateUser(View view, Map<String, Object> updatedata) {
        FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updatedata)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view,e.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(view,"Update information successfully",Snackbar.LENGTH_LONG).show();
            }
        });
    }
}

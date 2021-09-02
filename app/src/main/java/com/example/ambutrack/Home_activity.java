package com.example.ambutrack;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Home_activity extends AppCompatActivity {
    private Button button,button1;
    private LocationRequest locationRequest;
    //FirebaseAuth fAuth;
    public static final int REQUEST_CHECK_SETTING = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        getLocation();
        //Paper.init(this);
        button = (Button) findViewById(R.id.driver_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opendriverActivity();
            }
        });
        button1 = (Button) findViewById(R.id.patient_button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity();
            }
        });
        /*String Username = Paper.book().read(Prevalent.Username);
        String Password = Paper.book().read(Prevalent.Password);
        if(Username != "" && Password != ""){
            if(!TextUtils.isEmpty(Username) && !TextUtils.isEmpty(Password)){
                AllowAccess(Username,Password);
            }
        }*/
    }

    /*private void AllowAccess(final String mail, final String pass) {
        fAuth.signInWithEmailAndPassword(mail,pass)
                .addOnCompleteListener(Home_activity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(PatientLoginActivity.this,"Login Successfully",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainMapActivity.class));
                        } else {
                            Toast.makeText(Home_activity.this,"Login Failed, User doesn't Exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }*/

    private void getLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(Home_activity.this,REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException sendIntentException) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHECK_SETTING){
            switch (resultCode){
                case Activity.RESULT_OK:
                    break;
                case Activity.RESULT_CANCELED:
                    System.exit(0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Home_activity.super.onBackPressed();
                        System.exit(0);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void opendriverActivity(){
        Intent intent = new Intent(this,driverActivity.class);
        startActivity(intent);
    }
    public void loginActivity(){
        Intent intent = new Intent(this,PatientLoginActivity.class);
        startActivity(intent);
    }

    public void backArrow(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Home_activity.super.onBackPressed();
                        System.exit(0);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
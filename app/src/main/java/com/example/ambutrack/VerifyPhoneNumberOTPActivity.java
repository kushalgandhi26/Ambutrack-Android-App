package com.example.ambutrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumberOTPActivity extends AppCompatActivity {
    EditText enternumber;
    Button getotpbutton;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number_o_t_p);
        enternumber = findViewById(R.id.verifyPhone);
        getotpbutton = findViewById(R.id.getOtpButton);
        progressBar = findViewById(R.id.verifyotp1);
        getotpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!enternumber.getText().toString().trim().isEmpty()){
                    if((enternumber.getText().toString().trim()).length() == 10){
                        progressBar.setVisibility(View.VISIBLE);
                        getotpbutton.setVisibility(View.INVISIBLE);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+91" + enternumber.getText().toString(),
                                60,
                                TimeUnit.SECONDS,
                                VerifyPhoneNumberOTPActivity.this,
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        getotpbutton.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        getotpbutton.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String backendotp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        getotpbutton.setVisibility(View.INVISIBLE);
                                        Intent intent = new Intent(getApplicationContext(),DriverOTPVerificationActivity.class);
                                        intent.putExtra("mobile",enternumber.getText().toString());
                                        intent.putExtra("backendotp",backendotp);
                                        startActivity(intent);
                                    }
                                }
                        );

                        Intent intent = new Intent(getApplicationContext(),DriverOTPVerificationActivity.class);
                        intent.putExtra("mobile",enternumber.getText().toString());
                        startActivity(intent);
                    }else{
                        Toast.makeText(VerifyPhoneNumberOTPActivity.this,"Please Enter Correct Number",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(VerifyPhoneNumberOTPActivity.this,"Enter Mobile Number",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void backArrow(View view) {
        Intent intent = new Intent(getApplicationContext(),driverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),driverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}
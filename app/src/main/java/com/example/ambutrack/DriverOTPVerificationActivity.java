package com.example.ambutrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class DriverOTPVerificationActivity extends AppCompatActivity {
    EditText inputnumber1,inputnumber2,inputnumber3,inputnumber4,inputnumber5,inputnumber6;
    Button verifybuttonclick;
    String getotpbackend;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_o_t_p_verification);
        inputnumber1 = findViewById(R.id.inputotp1);
        inputnumber2 = findViewById(R.id.inputotp2);
        inputnumber3 = findViewById(R.id.inputotp3);
        inputnumber4 = findViewById(R.id.inputotp4);
        inputnumber5 = findViewById(R.id.inputotp5);
        inputnumber6 = findViewById(R.id.inputotp6);
        progressBar = findViewById(R.id.verifyotp2);
        verifybuttonclick = findViewById(R.id.getotpbutton2);

        TextView textView = findViewById(R.id.displayphno);
        textView.setText(String.format("+91-%s",getIntent().getStringExtra("mobile")));

        getotpbackend = getIntent().getStringExtra("backendotp");

        verifybuttonclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!inputnumber1.getText().toString().trim().isEmpty() && !inputnumber2.getText().toString().trim().isEmpty()
                        && !inputnumber3.getText().toString().trim().isEmpty() &&
                        !inputnumber4.getText().toString().trim().isEmpty() && !inputnumber5.getText().toString().trim().isEmpty()
                        && !inputnumber6.getText().toString().trim().isEmpty()){
                    String enterotpcode = inputnumber1.getText().toString()+
                            inputnumber2.getText().toString()+inputnumber3.getText().toString()+
                            inputnumber4.getText().toString()+inputnumber5.getText().toString()+inputnumber6.getText().toString();
                    if(getotpbackend!=null){
                        progressBar.setVisibility(View.VISIBLE);
                        verifybuttonclick.setVisibility(View.INVISIBLE);
                        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(getotpbackend,enterotpcode);
                        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        verifybuttonclick.setVisibility(View.INVISIBLE);
                                        if(task.isSuccessful()){
                                            Intent intent = new Intent(getApplicationContext(),DriverRegistrationActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Please Enter Correct OTP",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(getApplicationContext(),"Please Check Your Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                    //Toast.makeText(getApplicationContext(),"OTP Verify",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Please Enter All Number",Toast.LENGTH_SHORT).show();
                }
            }
        });
        numberotpmove();
    }

    private void numberotpmove() {
        inputnumber1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputnumber2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputnumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputnumber3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputnumber3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputnumber4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputnumber4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputnumber5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputnumber5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputnumber6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(DriverOTPVerificationActivity.this,"Keep Patience OTP will be sent Soon",Toast.LENGTH_SHORT).show();
    }

    public void ResendOTP(View view) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + getIntent().getStringExtra("mobile"),
                60,
                TimeUnit.SECONDS,
                DriverOTPVerificationActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newbackendotp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        getotpbackend = newbackendotp;
                        Toast.makeText(DriverOTPVerificationActivity.this,"OTP has been sent successfully",Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}
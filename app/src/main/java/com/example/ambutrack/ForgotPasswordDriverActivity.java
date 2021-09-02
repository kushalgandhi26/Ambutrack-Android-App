package com.example.ambutrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDriverActivity extends AppCompatActivity {


    EditText driver_forgot_email_id;
    Button driver_forgot_password_button;
    FirebaseAuth auth;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_driver);
        auth = FirebaseAuth.getInstance();
        driver_forgot_email_id = findViewById(R.id.driver_forgot_email_id);
        driver_forgot_password_button = findViewById(R.id.driver_forgot_password_button);
        driver_forgot_password_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        email = driver_forgot_email_id.getText().toString();
        if(email.isEmpty()){
            driver_forgot_email_id.setError("Required");
        }else {
            forgotPass();
        }
    }

    private void forgotPass() {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ForgotPasswordDriverActivity.this,"Check your Email",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ForgotPasswordDriverActivity.this,DriverLoginActivity.class));
                            finish();
                        }else{
                            Toast.makeText(ForgotPasswordDriverActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void backArrow(View view) {
        Intent intent = new Intent(getApplicationContext(),DriverLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),DriverLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}
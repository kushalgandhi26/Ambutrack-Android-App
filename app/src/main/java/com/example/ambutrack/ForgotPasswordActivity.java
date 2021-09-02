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

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText forgot_email_id;
    Button forgot_password_button;
    FirebaseAuth auth;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        auth = FirebaseAuth.getInstance();
        forgot_email_id = findViewById(R.id.forgot_email_id);
        forgot_password_button = findViewById(R.id.forgot_password_button);

        forgot_password_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        email = forgot_email_id.getText().toString();
        if(email.isEmpty()){
            forgot_email_id.setError("Required");
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
                            Toast.makeText(ForgotPasswordActivity.this,"Check your Email",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ForgotPasswordActivity.this,PatientLoginActivity.class));
                            finish();
                        }else{
                            Toast.makeText(ForgotPasswordActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    public void backArrow(View view) {
        Intent intent = new Intent(getApplicationContext(),PatientLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),PatientLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }
}
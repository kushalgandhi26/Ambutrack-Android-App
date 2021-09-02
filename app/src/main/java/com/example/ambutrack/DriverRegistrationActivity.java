package com.example.ambutrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ambutrack.Utils.UserUtils;
import com.example.ambutrack.ui.DriverInfoModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DriverRegistrationActivity extends AppCompatActivity {
    EditText name,number,gender,rcNumber,licenseNumber,mailId,password,repassword;
    RadioGroup radioGroup;
    RadioButton radioButton;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference,driverInfoRef;
    Button signup;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);
        name = findViewById(R.id.driver_username);
        number = findViewById(R.id.driver_contactnumber);
        rcNumber = findViewById(R.id.rcNumber);
        licenseNumber = findViewById(R.id.licenseNumber);
        mailId = findViewById(R.id.d_mailId);
        password = findViewById(R.id.d_password);
        repassword = findViewById(R.id.d_repassword);
        progressBar = findViewById(R.id.d_progressbar);
        radioGroup = findViewById(R.id.d_radioGroup);
        databaseReference = FirebaseDatabase.getInstance().getReference("Driver");
        firebaseAuth = FirebaseAuth.getInstance();
        signup = findViewById(R.id.driver_registerButton);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.onEditorAction(EditorInfo.IME_ACTION_DONE);
                number.onEditorAction(EditorInfo.IME_ACTION_DONE);
                rcNumber.onEditorAction(EditorInfo.IME_ACTION_DONE);
                licenseNumber.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mailId.onEditorAction(EditorInfo.IME_ACTION_DONE);
                password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                repassword.onEditorAction(EditorInfo.IME_ACTION_DONE);
                String userName = name.getText().toString();
                String num = number.getText().toString();
                String rc = rcNumber.getText().toString();
                String lc = licenseNumber.getText().toString();
                String mail = mailId.getText().toString();
                String pass = password.getText().toString();
                String rePass = repassword.getText().toString();
                int selectId = radioGroup.getCheckedRadioButtonId();
                String gender = Check(selectId);
                if(userName.equals("") || num.equals("") || rc.equals("") || lc.equals("") || mail.equals("") ||gender.equals("") || pass.equals("") || rePass.equals("")){
                    Toast.makeText(DriverRegistrationActivity.this,"Please Enter All Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(num.length() != 10){
                    Toast.makeText(DriverRegistrationActivity.this,"Please Enter The Correct Number",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pass.length() < 6){
                    Toast.makeText(DriverRegistrationActivity.this,"Password should be of Minimum 6 Character",Toast.LENGTH_SHORT).show();
                    return;
                }if(!pass.equals(rePass)){
                    Toast.makeText(DriverRegistrationActivity.this,"Password doesn't Match",Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.createUserWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(DriverRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    DriverInfoModel model = new DriverInfoModel();
                                    model.setName(userName);
                                    model.setPhno(num);
                                    model.setGender(gender);
                                    model.setRc(rc);
                                    model.setLc(lc);
                                    FirebaseDatabase.getInstance().getReference(Common.DRIVERS_INFO_REFERENCES)
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(model)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                    Driver data = new Driver(userName,num,gender,rc,lc,mail,pass,rePass);
                                    FirebaseDatabase.getInstance().getReference("Driver")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(DriverRegistrationActivity.this,"Registration Complete",Toast.LENGTH_SHORT).show();
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if(user != null){
                                                FirebaseInstanceId.getInstance()
                                                        .getInstanceId()
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(DriverRegistrationActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                        }).addOnSuccessListener(instanceIdResult -> {
                                                    Log.d("TOKEN",instanceIdResult.getToken());
                                                    UserUtils.updateToken(DriverRegistrationActivity.this,instanceIdResult.getToken());
                                                });
                                            }
                                            Intent intent = new Intent(getApplicationContext(),DriverMapActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                } else{
                                    Toast.makeText(DriverRegistrationActivity.this,"User Already Exist",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });

            }
        });
    }

    private String Check(int id) {
        String driverGender="";
        switch(id) {
            case R.id.d_female:
                driverGender = "Female";
                break;
            case R.id.d_male:
                driverGender = "Male";
                break;
        }
        return driverGender;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),driverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    public void backArrow(View view) {
        Intent intent = new Intent(getApplicationContext(),driverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}
package com.example.ambutrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ambutrack.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegistrationActivity extends AppCompatActivity {
    EditText username,number,address,mailid,password,repassword,day,month,year;
    RadioGroup radioGroup;
    String bloodt,dob;
    RadioButton radioButton;
    Spinner spinner;
    Button register;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    int day_i,month_i,year_i;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        username =  findViewById(R.id.username);
        number = findViewById(R.id.contact_number);
        //dob = findViewById(R.id.dob);
        address = findViewById(R.id.address);
        mailid = findViewById(R.id.mailid);
        password = findViewById(R.id.r_password);
        repassword = findViewById(R.id.r_repassword);
        radioGroup =  findViewById(R.id.radioGroup);
        day = findViewById(R.id.day);
        month = findViewById(R.id.month);
        year = findViewById(R.id.year);
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(RegistrationActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.BloodGroup));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bloodt = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        progressBar = findViewById(R.id.l_progressBar);
        ref = FirebaseDatabase.getInstance().getReference("User");
        fAuth = FirebaseAuth.getInstance();
        register = findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.onEditorAction(EditorInfo.IME_ACTION_DONE);
                number.onEditorAction(EditorInfo.IME_ACTION_DONE);
                //dob.onEditorAction(EditorInfo.IME_ACTION_DONE);
                address.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mailid.onEditorAction(EditorInfo.IME_ACTION_DONE);
                password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                repassword.onEditorAction(EditorInfo.IME_ACTION_DONE);
                String name = username.getText().toString();
                String num = number.getText().toString();
                //String birthdate = dob.getText().toString();
                //String bloodt = bloodtype.getText().toString();
                int selectId = radioGroup.getCheckedRadioButtonId();
                //radioButton = (RadioButton)findViewById(selectId);
                String gender = Check(selectId);
                String adds = address.getText().toString();
                String mail = mailid.getText().toString();
                String pass = password.getText().toString();
                String repass = repassword.getText().toString();
                if(!day.getText().toString().trim().isEmpty() && !month.getText().toString().trim().isEmpty() && !year.getText().toString().trim().isEmpty()){
                    dob = day.getText().toString()+"/"+month.getText().toString()+"/"+year.getText().toString();
                }else {
                    dob = "";
                }
                if(name.equals("") || num.equals("")  || dob.equals("") || bloodt.equals("") || gender.equals("") ||adds.equals("") || mail.equals("") || pass.equals("") || repass.equals("")){
                    Toast.makeText(RegistrationActivity.this,"Please Enter All Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(num.length() != 10){
                    Toast.makeText(RegistrationActivity.this,"Please Enter The Correct Number",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!day.getText().toString().trim().isEmpty()){
                    day_i = Integer.parseInt(day.getText().toString());
                    if(!(day_i>=1 && day_i<=31)){
                        Toast.makeText(RegistrationActivity.this,"Enter valid Day",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if(!month.getText().toString().trim().isEmpty()){
                    month_i = Integer.parseInt(month.getText().toString());
                    if(!(month_i>=1 && month_i<=12)){
                        Toast.makeText(RegistrationActivity.this,"Enter valid Month",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if(!year.getText().toString().trim().isEmpty()){
                    year_i = Integer.parseInt(year.getText().toString());
                    if(year_i<1900){
                        Toast.makeText(RegistrationActivity.this,"Enter valid Year",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if(pass.length() < 6){
                    Toast.makeText(RegistrationActivity.this,"Password should be of Minimum 6 Character",Toast.LENGTH_SHORT).show();
                    return;
                }if(!pass.equals(repass)){
                    Toast.makeText(RegistrationActivity.this,"Password doesn't Match",Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                fAuth.createUserWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    User data = new User(name,num,dob,bloodt,gender,adds,mail,pass,repass);
                                    FirebaseDatabase.getInstance().getReference("User")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(RegistrationActivity.this,"Registration Complete",Toast.LENGTH_SHORT).show();
                                            FirebaseUser user = fAuth.getCurrentUser();
                                            if(user != null){
                                                FirebaseInstanceId.getInstance()
                                                        .getInstanceId()
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(RegistrationActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                        }).addOnSuccessListener(instanceIdResult -> {
                                                    Log.d("TOKEN",instanceIdResult.getToken());
                                                    UserUtils.updateToken(RegistrationActivity.this,instanceIdResult.getToken());
                                                });
                                            }
                                            Intent intent = new Intent(getApplicationContext(),MainMapActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                } else{
                                    Toast.makeText(RegistrationActivity.this,"User Already Exist",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }


    public String Check(int id) {
        String userGender="";
        switch(id) {
            case R.id.female:
                userGender = "Female";
                break;
            case R.id.male:
                userGender = "Male";
                break;
        }
        return userGender;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),PatientLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
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
}
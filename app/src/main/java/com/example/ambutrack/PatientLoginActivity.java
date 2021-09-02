package com.example.ambutrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ambutrack.Database.SessionManager;
import com.example.ambutrack.Utils.UserUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class PatientLoginActivity extends AppCompatActivity {
    Button button1;
    private SignInButton signInButton;
    private GoogleSignInClient googleSignInClient;
    EditText emailId,password;
    ProgressBar progressBar;
    TextView forgot_password;
    private FirebaseAuth fAuth;
    private int RC_SIGN_IN = 1;
    private CheckBox rememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);
        emailId = findViewById(R.id.email_id);
        password = findViewById(R.id.driver_password);
        button1 = findViewById(R.id.signin_button);
        forgot_password = findViewById(R.id.forgot_password);
        rememberMe = findViewById(R.id.rememberMe);
        SessionManager sessionManager = new SessionManager(PatientLoginActivity.this,SessionManager.SESSION_REMEMBERME);
        if(sessionManager.checkRememberMe()){
            HashMap<String,String> rememberMeDetails = sessionManager.getRememberMeDetailsFromSession();
            emailId.setText(rememberMeDetails.get(SessionManager.KEY_SESSIONMAILID));
            password.setText(rememberMeDetails.get(SessionManager.KEY_PASSWORD));
        }
       /* GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
       /* googleSignInClient = GoogleSignIn.getClient(this,gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                emailId.onEditorAction(EditorInfo.IME_ACTION_DONE);
                password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                signIn();
            }
        });*/

        progressBar = (ProgressBar) findViewById(R.id.l_progressBar);
        fAuth = FirebaseAuth.getInstance();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailId.onEditorAction(EditorInfo.IME_ACTION_DONE);
                password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                String mail = emailId.getText().toString();
                String pass = password.getText().toString();

                if(mail.isEmpty() && pass.isEmpty()){
                    emailId.setError("Required");
                    password.setError("Required");
                    return;
                }
                if(mail.isEmpty()){
                    emailId.setError("Required");
                    return;
                }
                if(pass.isEmpty()){
                    password.setError("Required");
                    return;
                }

                if(rememberMe.isChecked()){
                    SessionManager sessionManager = new SessionManager(PatientLoginActivity.this,SessionManager.SESSION_REMEMBERME);
                    sessionManager.createRememberMeSession(mail,pass);
                }
                /*if(TextUtils.isEmpty(mail) && TextUtils.isEmpty(pass)){
                    Toast.makeText(PatientLoginActivity.this,"Please Enter Email and Password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(mail)){
                    Toast.makeText(PatientLoginActivity.this,"Please Enter Email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(pass)){
                    Toast.makeText(PatientLoginActivity.this,"Please Enter Password",Toast.LENGTH_SHORT).show();
                    return;
                }
                /*if(checkBox.isChecked()){
                    Paper.book().write(Prevalent.Username,mail);
                    Paper.book().write(Prevalent.Password,pass);
                }*/
                progressBar.setVisibility(View.VISIBLE);
                fAuth.signInWithEmailAndPassword(mail,pass)
                        .addOnCompleteListener(PatientLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(PatientLoginActivity.this,"Login Successfully",Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = fAuth.getCurrentUser();
                                    if(user != null){
                                        FirebaseInstanceId.getInstance()
                                                .getInstanceId()
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(PatientLoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                }).addOnSuccessListener(instanceIdResult -> {
                                            Log.d("TOKEN",instanceIdResult.getToken());
                                            UserUtils.updateToken(PatientLoginActivity.this,instanceIdResult.getToken());
                                        });
                                    }
                                    startActivity(new Intent(getApplicationContext(),MainMapActivity.class));
                                } else {
                                    Toast.makeText(PatientLoginActivity.this,"Login Failed, User doesn't Exist",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),Home_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    private void signIn(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private  void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            //Toast.makeText(PatientLoginActivity.this,"Signed In Successfully",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
           // startActivity(new Intent(getApplicationContext(),MainMapActivity.class));
        }catch (ApiException e){
            Toast.makeText(PatientLoginActivity.this,"Signed In Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acct){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        fAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(PatientLoginActivity.this,"Successful",Toast.LENGTH_SHORT).show();
                    FirebaseUser firebaseUser = fAuth.getCurrentUser();
                    upadateUI(firebaseUser);
                }else{
                    //Toast.makeText(PatientLoginActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                    upadateUI(null);
                }
            }
        });
    }

    private void upadateUI(FirebaseUser firebaseUser){
        startActivity(new Intent(getApplicationContext(),MainMapActivity.class));
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null){
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();
        }*/
    }

    public void openRegistrationActivity(){
        Intent intent = new Intent(this,RegistrationActivity.class);
        startActivity(intent);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    public void backArrow(View view) {
        Intent intent = new Intent(getApplicationContext(),Home_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void goToForgotPasswordActivity(View view) {
        Intent intent = new Intent(this,ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void goToRegistration(View view) {
        emailId.onEditorAction(EditorInfo.IME_ACTION_DONE);
        password.onEditorAction(EditorInfo.IME_ACTION_DONE);
        openRegistrationActivity();
    }
}
package com.example.ambutrack;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class MainMapActivity extends AppCompatActivity {

    private static final int PICK_REQUEST_IMAGE = 7172;
    private AppBarConfiguration mAppBarConfiguration;
    FirebaseAuth fAuth;
    DatabaseReference ref;
    private NavigationView navigationView;
    private NavController navController;
    private DrawerLayout drawer;
    StorageReference storageReference;
    private AlertDialog waitingDialog;
    private Uri imageUri;
    private ImageView avatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ref = FirebaseDatabase.getInstance().getReference("User")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fAuth = FirebaseAuth.getInstance();
        //storageReference = FirebaseStorage.getInstance().getReference();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_myprofile,R.id.nav_logout,R.id.nav_notification,R.id.nav_map)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int id = destination.getId();
                if (id == R.id.nav_logout) {
                    //Paper.book().destroy();
                    fAuth.signOut();
                    //Toast.makeText(MainMapActivity.this, "Successfully Logout", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), PatientLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        init();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_REQUEST_IMAGE && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() != null){
                imageUri = data.getData();
                avatar.setImageURI(imageUri);

                showDialogUpload();
            }
        }
    }

    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to change avatar?")
                .setCancelable(false)
                .setPositiveButton("UPLOAD", (dialog, which) -> {
                   if(imageUri != null){
                       waitingDialog.setMessage("Uploading...");
                       waitingDialog.show();

                       String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                       StorageReference userAvatarFolder = storageReference.child("avatars/"+unique_name);

                       userAvatarFolder.putFile(imageUri)
                               .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show())
                               .addOnCompleteListener(task -> {
                                   if(task.isSuccessful()){
                                       userAvatarFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                           Map<String,Object> updatedata = new HashMap<>();
                                           updatedata.put("avatar",uri.toString());
                                           UserUtils.updateUser(drawer,updatedata);
                                       });
                                   }
                                   waitingDialog.dismiss();
                               }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                                waitingDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                           }
                       });
                   }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }*/

    private void init() {
        /*waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Waiting...")
                .create();*/
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        View headerView = navigationView.getHeaderView(0);
        TextView name = headerView.findViewById(R.id.heyUser);
        TextView mail = headerView.findViewById(R.id.user_maiId);
        /*name.setText(String.format("Hey %s!",account.getDisplayName()));
        mail.setText(account.getEmail());*/
        //avatar = headerView.findViewById(R.id.profile_photo);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue().toString();
                String mailId = snapshot.child("mailid").getValue().toString();
                name.setText(String.format("Hey %s!",username));
                mail.setText(mailId);
                /*avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,PICK_REQUEST_IMAGE);
                    }
                });
                if(Common.currentPatient != null && Common.currentPatient.getAvatar() != null &&
                !TextUtils.isEmpty(Common.currentPatient.getAvatar())){
                    Glide.with(MainMapActivity.this)
                            .load(Common.currentPatient.getAvatar())
                            .into(avatar);
                }*/
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to Logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainMapActivity.super.onBackPressed();
                        //Paper.book().destroy();
                        fAuth.signOut();
                        Intent intent = new Intent(getApplicationContext(), PatientLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_map, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
package com.example.ambutrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ambutrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverProfile extends Fragment {

    private DriverProfileViewModel mViewModel;
    TextView name,phono,gender,lc,rc;

    public static DriverProfile newInstance() {
        return new DriverProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);
        name = view.findViewById(R.id.d_name);
        phono = view.findViewById(R.id.d_phno);
        gender = view.findViewById(R.id.d_gender);
        lc = view.findViewById(R.id.d_lic);
        rc = view.findViewById(R.id.d_rc);
        FirebaseDatabase.getInstance().getReference().child("Driver")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("name").getValue().toString();
                String number = snapshot.child("contactNumber").getValue().toString();
                String lic = snapshot.child("licenseNumber").getValue().toString();
                String rcN = snapshot.child("rcNumber").getValue().toString();
                String gend = snapshot.child("gender").getValue().toString();
                name.setText(username);
                phono.setText(number);
                gender.setText(gend);
                lc.setText(lic);
                rc.setText(rcN);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DriverProfileViewModel.class);
        // TODO: Use the ViewModel
    }

}
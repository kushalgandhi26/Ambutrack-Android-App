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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyProfile extends Fragment {

    private MyProfileViewModel mViewModel;
    TextView name,phono,birthDate,bloodGroup,gender,address;
    DatabaseReference databaseReference;

    public static MyProfile newInstance() {
        return new MyProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_profile_fragment, container, false);
        name = view.findViewById(R.id.p_name);
        phono = view.findViewById(R.id.p_phno);
        birthDate = view.findViewById(R.id.p_birthdate);
        bloodGroup = view.findViewById(R.id.p_bloodgroup);
        gender = view.findViewById(R.id.p_gender);
        address = view.findViewById(R.id.p_address);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue().toString();
                String number = snapshot.child("number").getValue().toString();
                String dob = snapshot.child("dob").getValue().toString();
                String bloodtype = snapshot.child("bloodtype").getValue().toString();
                String gend = snapshot.child("gender").getValue().toString();
                String add = snapshot.child("address").getValue().toString();
                name.setText(username);
                phono.setText(number);
                birthDate.setText(dob);
                bloodGroup.setText(bloodtype);
                gender.setText(gend);
                address.setText(add);
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
        mViewModel = new ViewModelProvider(this).get(MyProfileViewModel.class);
        // TODO: Use the ViewModel
    }

}
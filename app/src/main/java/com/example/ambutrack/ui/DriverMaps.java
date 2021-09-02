package com.example.ambutrack.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ambutrack.Common;
import com.example.ambutrack.EventBus.DriverRequestReceived;
import com.example.ambutrack.R;
import com.example.ambutrack.Utils.UserUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DriverMaps extends Fragment implements OnMapReadyCallback {

    private DriverMapsViewModel mViewModel;
    private GoogleMap mMap;
    private boolean isFirstTime = true;
    Chip chip_decline, complete_trip;
    CardView layout_accept;
    CircularProgressBar circularProgressBar;
    TextView txt_estimate_time;
    TextView txt_estimate_distance, request;
    private DriverRequestReceived driverRequestReceived;
    private Disposable countDownEvent;
    FrameLayout root_layout;

    SupportMapFragment mapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    DatabaseReference onlineRef, currentUserRef, driverLocationRef;
    GeoFire geoFire;
    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentUserRef != null) {
                currentUserRef.onDisconnect().removeValue();
                isFirstTime = true;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        //geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
        if (EventBus.getDefault().hasSubscriberForEvent(DriverRequestReceived.class))
            EventBus.getDefault().removeStickyEvent(DriverRequestReceived.class);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    public static DriverMaps newInstance() {
        return new DriverMaps();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(DriverMapsViewModel.class);
        View root = inflater.inflate(R.layout.driver_maps_fragment, container, false);
        chip_decline = root.findViewById(R.id.chip_decline);
        complete_trip = root.findViewById(R.id.complete_trip);
        layout_accept = root.findViewById(R.id.layout_accept);
        circularProgressBar = root.findViewById(R.id.circularProgresbar);
        txt_estimate_time = root.findViewById(R.id.txt_estimate_time);
        txt_estimate_distance = root.findViewById(R.id.txt_estimate_distance);
        request = root.findViewById(R.id.patient_request);
        root_layout = root.findViewById(R.id.root_layout);
        chip_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverRequestReceived != null) {
                    if (countDownEvent != null)
                        countDownEvent.dispose();
                    chip_decline.setVisibility(View.GONE);
                    layout_accept.setVisibility(View.GONE);
                    mMap.clear();
                    UserUtils.sendDeclineRequest(root_layout, getContext(), driverRequestReceived.getKey());
                    driverRequestReceived = null;
                } else {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    fusedLocationProviderClient.getLastLocation()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            driverRequestReceived = null;
                            makeDriverOnline(location);
                        }
                    });
                }
            }
        });

       /*complete_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> update_trip = new HashMap<>();
                update_trip.put("done", true);
                FirebaseDatabase.getInstance()
                        .getReference("DriverInfo")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(update_trip)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(aVoid -> {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(mapFragment.requireView(), getContext().getString(R.string.permission_require), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    fusedLocationProviderClient.getLastLocation()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(mapFragment.requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            UserUtils.sendCompleteTripToDriver(mapFragment.requireView(),getContext(),driverRequestReceived.getKey(),
                                    )
                        }
                    });
                        });
            }
        });*/
        initViews(root);
        init();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_driver);
        mapFragment.getMapAsync(this);
        return root;
    }

    private void initViews(View root) {
        ButterKnife.bind(this, root);
    }

    private void init() {
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Snackbar.make(root_layout,getString(R.string.permission_require),Snackbar.LENGTH_SHORT).show();
            Toast.makeText(getContext(), R.string.permission_require, Toast.LENGTH_SHORT).show();
            return;
        }

        buildLocationRequest();
        buildLocationCallback();
        updateLocation();

    }

    private void updateLocation() {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void buildLocationCallback() {
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude()
                            , locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                    makeDriverOnline(locationResult.getLastLocation());

                }
            };
        }
    }

    private void makeDriverOnline(Location location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocation(location.getLatitude()
                    , location.getLongitude(), 1);
            String cityName = addressList.get(0).getLocality();

            driverLocationRef = FirebaseDatabase.getInstance().getReference(Common.DRIVERS_LOCATION_REFERENCES)
                    .child(cityName);
            currentUserRef = driverLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            geoFire = new GeoFire(driverLocationRef);

            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    new GeoLocation(location.getLatitude(),
                            location.getLongitude()),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
                            } else {
                                if (isFirstTime) {
                                    Snackbar.make(mapFragment.getView(), "You are online!", Snackbar.LENGTH_LONG).show();
                                    isFirstTime = false;
                                }
                            }
                        }
                    });
            registerOnlineSystem();
        } catch (IOException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void buildLocationRequest() {
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(50f);
            locationRequest.setInterval(15000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return false;
                            }
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e -> Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));
                                    });

                            return true;
                        });

                        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 50);

                        buildLocationRequest();
                        buildLocationCallback();
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(mapFragment.getView(), permissionDeniedResponse.getPermissionName() + "need enable",
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.uber_maps_style));
            if (!success)
                Snackbar.make(mapFragment.getView(), "Load map style failed", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverRequestReceive(DriverRequestReceived event) {
        driverRequestReceived = event;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(requireView(), getString(R.string.permission_require), Snackbar.LENGTH_LONG).show();
            return;
        }

        /*FirebaseDatabase.getInstance().getReference("Request")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        txt_estimate_distance.setText(snapshot.child("name").getValue().toString());
                        txt_estimate_time.setText(snapshot.child("number").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });*/

        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show())
                .addOnSuccessListener(location -> {
                    LatLng origin = new LatLng(location.getLatitude(),location.getLongitude());
                    chip_decline.setVisibility(View.VISIBLE);
                    layout_accept.setVisibility(View.VISIBLE);
                    //complete_trip.setVisibility(View.VISIBLE);
                    countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(x -> {
                                circularProgressBar.setProgress(circularProgressBar.getProgress()+1f);
                            })
                            .takeUntil(aLong -> aLong == 100)
                            .doOnComplete(() -> {
                                circularProgressBar.setProgress(0);
                                FirebaseDatabase.getInstance().getReference("Request")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                               String temp = snapshot.child("key").getValue().toString();
                                               getPatientInfo(snapshot.child("userId").getValue().toString());
                                               chip_decline.setVisibility(View.GONE);
                                               request.setText("Request Accepted");
                                               try {
                                                   String uri = "http://maps.google.com/maps?daddr=" + temp;
                                                   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                                   intent.setPackage("com.google.android.apps.maps");
                                                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                   startActivity(intent);
                                               }catch (ActivityNotFoundException e){
                                                   String uri = "http://maps.google.com/maps?daddr=" + temp;
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }).subscribe();
                });
    }

    private void getPatientInfo(String userId) {
        FirebaseDatabase.getInstance().getReference("User")
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //txt_estimate_distance.setText(snapshot.child("username").getValue().toString());
                        //txt_estimate_time.setText(snapshot.child("number").getValue().toString());
                        txt_estimate_distance.setText(String.format("User's Name: %s",snapshot.child("username").getValue().toString()));
                        txt_estimate_time.setText(String.format("User's Contact No.: %s",snapshot.child("number").getValue().toString()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
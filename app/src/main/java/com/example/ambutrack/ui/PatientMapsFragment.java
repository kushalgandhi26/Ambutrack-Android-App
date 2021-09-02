package com.example.ambutrack.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ambutrack.Callback.IFirebaseDriverInfoListener;
import com.example.ambutrack.Callback.IFirebaseFailedListener;
import com.example.ambutrack.Common;
import com.example.ambutrack.EventBus.DeclineRequestFromDriver;
import com.example.ambutrack.R;
import com.example.ambutrack.Utils.UserUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PatientMapsFragment extends Fragment implements OnMapReadyCallback, IFirebaseFailedListener, IFirebaseDriverInfoListener {
    /*CardView layout_nearby_ambulance;
    View fill_maps;
    CardView finding_your_ride_layout;*/
    private HomeViewModel homeViewModel;
    private GoogleMap mMap;
    RelativeLayout patient_map;
    ConstraintLayout waiting_layout;
    ConstraintLayout on_success_layout;
    FrameLayout map_patient,confirm_layout;
    int flag = 0;
    /*Marker marker;
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPulseAnimator;
    private ValueAnimator animator;
    private static final int DESIRED_NUM_OF_SPINS = 5;
    private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 40;*/
    SupportMapFragment mapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Button findNearbyDriver;
    LatLng destination;
    TextView estimate_distance;
    LocationCallback locationCallback;
    DriverGeoModel foundDriver;
    TextView driverName,driverPhno;
    private boolean firstTime = true;
    private double distance = 1.0;
    private static final double LIMIT_RANGE = 10.0;
    private Location previousLocation, currentLocation;

    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;
    private DriverGeoModel lastDriverCall;


    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDriver.class))
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDriver.class);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_patient_maps, container, false);
        patient_map = root.findViewById(R.id.patient_map);
        waiting_layout = root.findViewById(R.id.waiting_layout);
        on_success_layout = root.findViewById(R.id.on_success_layout);
        map_patient = root.findViewById(R.id.map_patient);
        confirm_layout = root.findViewById(R.id.confirm_layout);
        driverName = root.findViewById(R.id.display_driver_name);
        driverPhno = root.findViewById(R.id.display_driver_phno);
        estimate_distance = root.findViewById(R.id.distance);
        /*layout_nearby_ambulance = root.findViewById(R.id.layout_nearby_ambulance);
        fill_maps = root.findViewById(R.id.fill_maps);
        finding_your_ride_layout = root.findViewById(R.id.finding_your_ride_layout);*/
        findNearbyDriver = root.findViewById(R.id.nearby_button);
        findNearbyDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(getView(),getString(R.string.permission_require),Snackbar.LENGTH_LONG).show();
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                destination = new LatLng(location.getLatitude(), location.getLongitude());
                                findNearbyDriver(destination);
                                if(foundDriver != null){
                                    driverName.setText(foundDriver.getDriverInfoModel().getName());
                                    driverPhno.setText(foundDriver.getDriverInfoModel().getPhno());
                                    //mMap.clear();
                                    map_patient.setVisibility(View.GONE);
                                    waiting_layout.setVisibility(View.VISIBLE);
                                    findNearbyDriver.setText("Getting Ambulance...");
                                    Observable.interval(100, TimeUnit.MILLISECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .takeUntil(aLong -> aLong == 120)
                                            .doOnComplete(() -> {
                                                if(flag!=1){
                                                    waiting_layout.setVisibility(View.GONE);
                                                    confirm_layout.setVisibility(View.GONE);
                                                    on_success_layout.setVisibility(View.VISIBLE);
                                                }
                                            }).subscribe();

                                }else{
                                    //Toast.makeText(map_patient.getContext(),"Drivers Not Found!",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
        return root;
    }


    /*private void addMarkerWithPuleAnimation() {
        layout_nearby_ambulance.setVisibility(View.GONE);
        fill_maps.setVisibility(View.VISIBLE);
        finding_your_ride_layout.setVisibility(View.VISIBLE);

        marker = mMap.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.defaultMarker())
        .position(destination));

        addPulsatingEffect(destination);

    }*/

    /*private void addPulsatingEffect(LatLng destination) {
        if(lastPulseAnimator != null) lastPulseAnimator.cancel();
        if(lastUserCircle != null) lastUserCircle.setCenter(destination);

        lastPulseAnimator = Common.valueAnimate(duration,animation -> {
            if(lastUserCircle != null) lastUserCircle.setRadius((Float)animation.getAnimatedValue());
            else{
                lastUserCircle = mMap.addCircle(new CircleOptions()
                .center(destination)
                .radius((Float)animation.getAnimatedValue())
                .strokeColor(Color.WHITE)
                .fillColor(Color.parseColor("#33333333")));
            }
        });
        startMapCameraSpinningAnimation(mMap.getCameraPosition().target);
    }*/

    /*private void startMapCameraSpinningAnimation(LatLng target) {
        if(animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0,DESIRED_NUM_OF_SPINS*360);
        animator.setDuration(DESIRED_SECONDS_PER_ONE_FULL_360_SPIN*DESIRED_NUM_OF_SPINS*1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(100);
        animator.addUpdateListener(animation -> {
                Float newBearingValue = (Float) animation.getAnimatedValue();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(target)
                .zoom(16f)
                .tilt(45f)
                .bearing(newBearingValue)
                .build()));
        });
        animator.start();
    }*/

    /*@Override
    public void onDestroyView() {
        if(animator != null) animator.end();
        super.onDestroyView();
    }*/

    private void findNearbyDriver(LatLng destination) {

        if(Common.driversFound.size() > 0){
            float min_distance = 0;
            foundDriver = null;
            Location currentRiderLocation = new Location("");
            currentRiderLocation.setLatitude(destination.latitude);
            currentRiderLocation.setLongitude(destination.longitude);
            for(String key : Common.driversFound.keySet()){
                Location driverLocation = new Location("");
                driverLocation.setLatitude(Common.driversFound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Common.driversFound.get(key).getGeoLocation().longitude);
                if(min_distance == 0){
                    min_distance = driverLocation.distanceTo(currentRiderLocation);
                    if(!Common.driversFound.get(key).isDecline()){
                        foundDriver = Common.driversFound.get(key);
                        break;
                    }else
                        continue;
                }else if(driverLocation.distanceTo(currentRiderLocation) < min_distance){
                    min_distance = driverLocation.distanceTo(currentRiderLocation);
                    if(!Common.driversFound.get(key).isDecline()){
                        foundDriver = Common.driversFound.get(key);
                        break;
                    }else
                        continue;
                }
                /*Snackbar.make(getView(),new StringBuilder("Found Driver: ").append(foundDriver.getDriverInfoModel().getPhno()),
                        Snackbar.LENGTH_LONG).show();*/
            }
            if(foundDriver != null){
                UserUtils.sendRequestToDriver(getContext(),patient_map,foundDriver,destination);
                float[] result = new float[1];
                Location.distanceBetween(foundDriver.getGeoLocation().latitude,foundDriver.getGeoLocation().longitude,
                        destination.latitude,destination.longitude,result);
                float distance = result[0];
                float distanceInKilometer = (distance/1000);
                estimate_distance.setText(String.valueOf(" "+distanceInKilometer+" km"));
                lastDriverCall = foundDriver;
            }else {
                Snackbar.make(map_patient,getString(R.string.no_driver_accept),Snackbar.LENGTH_LONG).show();
                waiting_layout.setVisibility(View.GONE);
                map_patient.setVisibility(View.VISIBLE);
                findNearbyDriver.setText("Find Nearby Ambulance");
                flag=1;
                lastDriverCall = null;
            }
        }else{
            Snackbar.make(map_patient,getString(R.string.drivers_not_found),Snackbar.LENGTH_LONG).show();
            flag=1;
            lastDriverCall = null;
        }
        if(foundDriver != null){
            String lat = Double.toString(destination.latitude);
            String lng = Double.toString(destination.longitude);
            String location = lat+ " "+ lng;
            PatientGeoModel patientGeoModel = new PatientGeoModel();
            patientGeoModel.setKey(location);
            patientGeoModel.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            FirebaseDatabase.getInstance().getReference("Request").child(foundDriver.getKey()).setValue(patientGeoModel);
            /*User user = new User();
            FirebaseDatabase.getInstance().getReference().child("User")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user.setUsername(snapshot.child("username").getValue().toString());
                            user.setNumber(snapshot.child("number").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
            FirebaseDatabase.getInstance().getReference("Request").child(foundDriver.getKey()).child("name").setValue(user.getUsername());
            FirebaseDatabase.getInstance().getReference("Request").child(foundDriver.getKey()).child("number").setValue(user.getNumber());*/
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onDeclineRequestEvent(DeclineRequestFromDriver event){
        if(lastDriverCall != null){
            Common.driversFound.get(lastDriverCall.getKey()).setDecline(true);
            findNearbyDriver(destination);
        }
    }


    private void init() {

        iFirebaseFailedListener = this;
        iFirebaseDriverInfoListener = this;

        if(ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            Snackbar.make(map_patient,getString(R.string.permission_require),Snackbar.LENGTH_SHORT).show();
            return;
        }

        buildLocationRequest();
        buildLocationCallback();
        updateLocation();


        loadAvailableDrivers();
    }

    private void updateLocation() {
        if(fusedLocationProviderClient == null){
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void buildLocationCallback() {
        if(locationCallback == null){
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude()
                            , locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                    if (firstTime) {
                        previousLocation = currentLocation = locationResult.getLastLocation();
                        firstTime = false;
                    } else {
                        previousLocation = currentLocation;
                        currentLocation = locationResult.getLastLocation();
                    }
                    if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) {
                        loadAvailableDrivers();
                    } else {

                    }
                }
            };
        }
    }

    private void buildLocationRequest() {
        if(locationRequest == null){
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(10f);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private void loadAvailableDrivers() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(map_patient,getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(map_patient, e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List <Address> addressList;
                    try {
                        addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        cityName = addressList.get(0).getLocality();

                        DatabaseReference driver_location_ref = FirebaseDatabase.getInstance()
                                .getReference(Common.DRIVERS_LOCATION_REFERENCES)
                                .child(cityName);
                        GeoFire gf = new GeoFire(driver_location_ref);
                        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.getLatitude(),
                                location.getLongitude()),distance);
                        geoQuery.removeAllListeners();
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                //Common.driversFound.add(new DriverGeoModel(key,location));
                                if(!Common.driversFound.containsKey(key)){
                                    Common.driversFound.put(key,new DriverGeoModel(key,location));
                                }
                            }

                            @Override
                            public void onKeyExited(String key) {

                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {

                            }

                            @Override
                            public void onGeoQueryReady() {
                                if(distance <= LIMIT_RANGE){
                                    distance++;
                                    loadAvailableDrivers();
                                }else {
                                    distance = 1.0;
                                    addDriverMarker();
                                }
                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                Snackbar.make(map_patient,error.getMessage(),Snackbar.LENGTH_SHORT).show();
                            }
                        });

                        driver_location_ref.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                                GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                        geoQueryModel.getL().get(1));
                                DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(),
                                        geoLocation);
                                Location newDriverLocation = new Location("");
                                newDriverLocation.setLatitude(geoLocation.latitude);
                                newDriverLocation.setLongitude(geoLocation.longitude);
                                float newDitance = location.distanceTo(newDriverLocation)/1000;
                                if(newDitance <= LIMIT_RANGE)
                                    findDriverByKey(driverGeoModel);
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }catch (IOException e){
                        e.printStackTrace();
                        Snackbar.make(map_patient,e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDriverMarker() {
        if(Common.driversFound.size() > 0){
            Observable.fromIterable(Common.driversFound.keySet())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(key -> {
                        findDriverByKey(Common.driversFound.get(key));
                    },throwable -> {
                        Snackbar.make(map_patient,throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
                    },()->{

                    });
        }else {
            Snackbar.make(map_patient,getString(R.string.drivers_not_found),Snackbar.LENGTH_LONG).show();
        }
    }


    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVERS_INFO_REFERENCES)
                .child(driverGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()){
                            driverGeoModel.setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            Common.driversFound.get(driverGeoModel.getKey()).setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            iFirebaseDriverInfoListener.onDriverInfoLoadSuccess(driverGeoModel);
                        }else {
                            iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.not_found_key)+driverGeoModel.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFirebaseFailedListener.onFirebaseLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mMap = googleMap;
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(map_patient,getString(R.string.permission_require),Snackbar.LENGTH_SHORT).show();
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
                                    .addOnFailureListener(e -> Snackbar.make(map_patient, e.getMessage(), Snackbar.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f));
                                    });

                            return true;
                        });

                        View locationButton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins(0,0,0,250);

                        buildLocationRequest();
                        buildLocationCallback();
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(map_patient,permissionDeniedResponse.getPermissionName()+"need enable",
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();
        mMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.uber_maps_style));
            if(!success)
                Snackbar.make(map_patient,"Load map style failed",Snackbar.LENGTH_SHORT).show();
        }catch (Exception e){
            Snackbar.make(map_patient,e.getMessage(),Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(map_patient,message,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        if(!Common.markerList.containsKey(driverGeoModel.getKey()))
            Common.markerList.put(driverGeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(driverGeoModel.getGeoLocation().latitude,driverGeoModel.getGeoLocation().longitude))
                    .flat(true)
                    .title(Common.buildName(driverGeoModel.getDriverInfoModel().getName()))
                    .snippet(driverGeoModel.getDriverInfoModel().getPhno())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_1))));
        if(!TextUtils.isEmpty(cityName)){
            DatabaseReference driverLocation = FirebaseDatabase.getInstance()
                    .getReference(Common.DRIVERS_LOCATION_REFERENCES)
                    .child(cityName)
                    .child(driverGeoModel.getKey());
            driverLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.hasChildren()){
                        if(Common.markerList.get(driverGeoModel.getKey()) != null){
                            Common.markerList.get(driverGeoModel.getKey()).remove();
                            Common.markerList.remove(driverGeoModel.getKey());
                            if(Common.driversFound != null && Common.driversFound.size() > 0)
                                Common.driversFound.remove(driverGeoModel.getKey());
                            driverLocation.removeEventListener(this);
                        }
                    }else {
                        if(Common.markerList.get(driverGeoModel.getKey()) != null){
                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                            AnimationModel animationModel = new AnimationModel(false,geoQueryModel);
                            if(Common.driverLocationSubscribe.get(driverGeoModel.getKey()) != null){
                                Marker currentMarker = Common.markerList.get(driverGeoModel.getKey());
                                AnimationModel oldPosition = Common.driverLocationSubscribe.get(driverGeoModel.getKey());
                            }else {
                                Common.driverLocationSubscribe.put(driverGeoModel.getKey(),animationModel);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(map_patient,error.getMessage(),Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

}

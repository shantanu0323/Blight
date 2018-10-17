package com.sada.blight;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = "HomeActivity";
    private static final int LOADER_ID = 2;
    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 24;
    private static final int REQUEST_CHECK_SETTINGS = 21;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private String location;
    private double lat;
    private double lon;
    private String QUERY_URL;
    private String BASE_URL = "https://blight-backend.herokuapp.com/query?location=";
    private boolean alreadyLoaded = false;

    private TextView tvTemp, tvPressure, tvVisibility, tvWind, tvHumidity;
    private LinearLayout dataContainer;
    private boolean showAlert = false;
    private LinearLayout alertContainer;
    private Animation blinkAnimation;
    private String alertTitle;
    private String alertMessage;
    private Button bHelpMe, bCancelAlert;
    private View snackbarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();
        bCancelAlert.setVisibility(View.GONE);
        bHelpMe.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        final String title, message;
        showAlert = getIntent().getBooleanExtra("showAlert", false);
        if (showAlert) {
            title = (getIntent().getStringExtra("title")).substring(5);
            message = getIntent().getStringExtra("message");
            ;
        } else {
            title = "Earthquake Alert";
            message = "Alert has been issued in your area, Stay careful";
        }
        DatabaseReference alertedUsersRef = FirebaseDatabase.getInstance().getReference().child("alerted_users");
        alertedUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
//                    Toast.makeText(HomeActivity.this, "ADDED", Toast.LENGTH_SHORT).show();
                    operationAlert(title, message, true);
                } else {
//                    alertContainer.setVisibility(View.GONE);
                    operationAlert(title, message, false);
//                    Toast.makeText(HomeActivity.this, "DELETED", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ((ImageButton) findViewById(R.id.bLogout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle locationDetails = fetchLocation();

        bHelpMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarView = getCurrentFocus();
                final Bundle locationDetails = fetchLocation();
                DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(mAuth.getCurrentUser().getUid());
                currentUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("contact")) {
                            String contact = dataSnapshot.child("contact").getValue().toString();
                            DatabaseReference alertRef = FirebaseDatabase.getInstance().getReference().child("alerts");

                            final HashMap<String, String> alertMap = new HashMap<String, String>();
                            alertMap.put("location", locationDetails.getString("location"));
                            alertMap.put("lat", locationDetails.getString("lat"));
                            alertMap.put("lon", locationDetails.getString("lon"));
                            alertMap.put("contact", contact);

                            alertRef.child(mAuth.getCurrentUser().getUid()).setValue(alertMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(findViewById(R.id.bHelpMe),
                                                    "Alert has been sent to the Authority, Stay put we will be there soon",
                                                    Snackbar.LENGTH_LONG).show();
                                            bHelpMe.setText("Approaching you");
                                            bHelpMe.setClickable(false);
                                            bHelpMe.setTextSize(20f);
                                            Drawable img = getResources().getDrawable(R.drawable.ic_approching);
                                            bHelpMe.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                                            bHelpMe.setBackground(getResources().getDrawable(R.drawable.bg_approaching));
                                            bHelpMe.setTextColor(Color.rgb(20, 150, 80));
                                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim
                                                    .blink);
                                            bHelpMe.startAnimation(animation);

                                            // Log the alert to the help_requests
                                            DatabaseReference helpRequestsRef = FirebaseDatabase.getInstance().getReference().child("help_requests");
                                            Calendar calendar = Calendar.getInstance();
                                            Long currentTimeInMillis = calendar.getTimeInMillis();
                                            String loggingKey = "millis_" + currentTimeInMillis;
                                            Date date = calendar.getTime();
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                                            String dateStr = sdf.format(date);
                                            sdf = new SimpleDateFormat("hh:mm:ss");
                                            String timeStr = sdf.format(date);
                                            alertMap.put("date", dateStr);
                                            alertMap.put("time", timeStr);
                                            alertMap.put("uid", mAuth.getCurrentUser().getUid());
                                            helpRequestsRef.child(loggingKey).setValue(alertMap);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        final DatabaseReference alertRef = FirebaseDatabase.getInstance().getReference().child("alerts");
        alertRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                    Snackbar.make(findViewById(R.id.bHelpMe),
                            "Alert has been sent to the Authority, Stay put we will be there soon",
                            Snackbar.LENGTH_LONG).show();
                    bHelpMe.setText("Approaching you");
                    bHelpMe.setClickable(false);
                    bHelpMe.setTextSize(20f);
                    Drawable img = getResources().getDrawable(R.drawable.ic_approching);
                    bHelpMe.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    bHelpMe.setBackground(getResources().getDrawable(R.drawable.bg_approaching));
                    bHelpMe.setTextColor(Color.rgb(20, 150, 80));
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim
                            .blink);
                    bHelpMe.startAnimation(animation);
                    bCancelAlert.setVisibility(View.VISIBLE);
                    bHelpMe.setVisibility(View.VISIBLE);
                } else {
                    bCancelAlert.setVisibility(View.GONE);
                    bHelpMe.setText("Help Me");
                    bHelpMe.setClickable(true);
                    bHelpMe.setTextSize(20f);
                    Drawable img = getResources().getDrawable(R.drawable.ic_help_me);
                    bHelpMe.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    bHelpMe.setBackground(getResources().getDrawable(R.drawable.bg_help_me));
                    bHelpMe.setTextColor(Color.rgb(228, 237, 64));
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                    bHelpMe.startAnimation(animation);
                    bHelpMe.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bCancelAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertRef.child(mAuth.getCurrentUser().getUid()).removeValue();
                Snackbar.make(findViewById(R.id.bHelpMe),
                        "Alert has been terminated",
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private Bundle fetchLocation() {
        Bundle locationDetails = new Bundle();
        GPSTracker gps = new GPSTracker(HomeActivity.this);
        if (gps.canGetLocation()) {
            String output = "";
            lat = gps.getLatitude();
            lon = gps.getLongitude();
            locationDetails.putString("lat", lat + "");
            locationDetails.putString("lon", lon + "");
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            location = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                Address obj = addresses.get(0);//.getAddressLine(0);
                location = obj.getLocality();
                locationDetails.putString("location", location);
                QUERY_URL = BASE_URL + location;
                initiateLoader();
                Toast.makeText(getApplicationContext(), location, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("location", location);
//                startActivity(intent);
//                finish();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return locationDetails;
    }

    private void operationAlert(String alertTitle, String alertMessage, boolean showAlert) {
        ((TextView) findViewById(R.id.tvAlertTitle)).setText(alertTitle);
        ((TextView) findViewById(R.id.tvAlertMessage)).setText(alertMessage);

        alertContainer.setVisibility(showAlert ? View.VISIBLE : View.GONE);
        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        alertContainer.startAnimation(blinkAnimation);
    }

    private void findViews() {
        alertContainer = findViewById(R.id.alertContainer);
        tvTemp = findViewById(R.id.tvTemp);
        tvPressure = findViewById(R.id.tvPressure);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvVisibility = findViewById(R.id.tvVisibility);
        dataContainer = findViewById(R.id.dataContainer);
        bHelpMe = findViewById(R.id.bHelpMe);
        bCancelAlert = findViewById(R.id.bCancelAlert);
    }

    private void initiateLoader() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.

            LoaderManager loaderManager = getSupportLoaderManager();
            if (alreadyLoaded) {
                loaderManager.restartLoader(LOADER_ID, null, this);
            } else {
                loaderManager.initLoader(LOADER_ID, null, this);
                alreadyLoaded = true;
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        final List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

        DatabaseReference rescueWorkers = FirebaseDatabase.getInstance().getReference().child("rescueworkers");
        rescueWorkers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Map<String, HashMap<String, String>> td = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
//                List<HashMap<String, String>> objectList = (List<HashMap<String, String>>) td.values();
//                for (HashMap<String, String> obj : objectList) {
//                    String rescueLot = obj.get("lat");
//                    String rescueLon = obj.get("lon");
//                    String region = obj.get("region");
//                    if (region.equals(location)) {
//
//                    }
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_ACCESS_COARSE_LOCATION);

        } else {

            displayLocationSettingsRequest(this);
            mMap.setMyLocationEnabled(true);
            // Add a marker in Sydney and move the camera
            LatLng position = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
            position = new LatLng(lat - 0.001d, lon - 0.001);
            mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
            position = new LatLng(lat + 0.003d, lon + 0.0015);
            mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
            position = new LatLng(lat - 0.004d, lon - 0.006);
            mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
            position = new LatLng(lat + 0.001d, lon + 0.003);
            mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {

        return new DataLoader(this, QUERY_URL);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        String output = "Fetched data : " + data;
        Log.i(TAG, "onLoadFinished: " + output);
//        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
        WeatherData weatherData = new WeatherData();
        weatherData = QueryUtils.extractFeatureFromJson(data);
//        Toast.makeText(getApplicationContext(), "" + weatherData.getForPD3(), Toast.LENGTH_SHORT).show();
        try {
            tvTemp.setText((weatherData.getTemp().toString().substring(0, 2)));
            tvPressure.setText(weatherData.getPressure().toString().substring(0, 4));
            tvHumidity.setText(weatherData.getHumidity().toString().substring(0, 3));
            tvVisibility.setText(weatherData.getVisibility().toString().substring(0, 3));
            tvWind.setText(weatherData.getWind().toString().substring(0, 3));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mMap.setMyLocationEnabled(true);
                    // Add a marker in Sydney and move the camera
                    LatLng position = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
                    position = new LatLng(lat - 0.001d, lon - 0.001);
                    mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
                    position = new LatLng(lat + 0.003d, lon + 0.0015);
                    mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
                    position = new LatLng(lat - 0.004d, lon - 0.006);
                    mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
                    position = new LatLng(lat + 0.001d, lon + 0.003);
                    mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
                } else {
                    Toast.makeText(getApplicationContext(), "This application requires the access to location in order to function", Toast.LENGTH_LONG).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}

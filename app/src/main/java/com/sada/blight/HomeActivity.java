package com.sada.blight;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private String location;
    private double lat;
    private double lon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        ((FloatingActionButton) findViewById(R.id.bLogout)).setOnClickListener(new View.OnClickListener() {
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

        GPSTracker gps = new GPSTracker(HomeActivity.this);
        if (gps.canGetLocation()) {
            String output = "";
            lat = gps.getLatitude();
            lon = gps.getLongitude();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            location = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
//                Toast.makeText(getApplicationContext(), "" + (addresses.get(0).getAddressLine(0) == null), Toast.LENGTH_SHORT).show();
                Address obj = addresses.get(0);//.getAddressLine(0);
                location = obj.getLocality();
                Toast.makeText(getApplicationContext(), location, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("location", location);
//                startActivity(intent);
//                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng position = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
        position = new LatLng(lat - 0.001d, lon - 0.001);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
        position = new LatLng(lat + 0.002d, lon + 0.0015);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
        position = new LatLng(lat - 0.0008d, lon - 0.0008);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
        position = new LatLng(lat + 0.0007d, lon - 0.0007);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in " + location));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
    }
}

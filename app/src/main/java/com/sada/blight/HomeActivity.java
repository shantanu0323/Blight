package com.sada.blight;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;


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

        GPSTracker gps = new GPSTracker(HomeActivity.this);
        if (gps.canGetLocation()) {
            String output = "";
            double lat = gps.getLatitude();
            double lon = gps.getLongitude();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String location = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
//                Toast.makeText(getApplicationContext(), "" + (addresses.get(0).getAddressLine(0) == null), Toast.LENGTH_SHORT).show();
                location = addresses.get(0).getAddressLine(0);
                Toast.makeText(getApplicationContext(), location, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("location", location);
                startActivity(intent);
                finish();
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
}

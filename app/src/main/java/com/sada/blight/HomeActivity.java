package com.sada.blight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = "HomeActivity";
    private static final int LOADER_ID = 2;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();
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
            }
        }
    }

    private void findViews() {
        tvTemp = findViewById(R.id.tvTemp);
        tvPressure = findViewById(R.id.tvPressure);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvVisibility = findViewById(R.id.tvVisibilty);
        dataContainer = findViewById(R.id.dataContainer);
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
        tvTemp.setText(weatherData.getTemp() + " deg C");
        tvPressure.setText(weatherData.getPressure() + "millibars");
        tvHumidity.setText(weatherData.getHumidity() + "");
        tvVisibility.setText(weatherData.getVisibility() + " km");
        tvWind.setText(weatherData.getWind() + "km/hr");
        dataContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}

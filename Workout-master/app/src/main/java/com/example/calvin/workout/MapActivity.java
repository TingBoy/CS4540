package com.example.calvin.workout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by FugiBeast on 8/5/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    GoogleMap mMap;
    ArrayList<LatLng> points;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Location oldLocation;
    double distance = 0d;
    double distanceCovered = 0;
    private Marker mCurrLocationMarker;

    private final LatLng mDefaultLocation = new LatLng(-122.084, 37.422); //Googleplex
    private boolean mLocationPermissionGranted;
    private boolean mRequestingLocationUpdates;
    private static final long LOCATION_REQUEST_INTERVAL = 2000;
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000;
    private CountDownTimer mCDTimer;
    Chronometer myChronometer;


    ArrayList<LatLng> routePoints;
    private Polyline line;
    Button startBtn;
    TextView calcDist;
    TextView caloriesBurnt;
    double totalCalories = 0;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        points = new ArrayList<LatLng>();

        mRequestingLocationUpdates = true;

        startBtn = (Button) findViewById(R.id.startBtn);
        myChronometer = (Chronometer)findViewById(R.id.trackChrono);
        calcDist = (TextView)findViewById(R.id.calcDistance);
        calcDist.setText("0.00 meters");
        caloriesBurnt = (TextView)findViewById(R.id.caloriesBurnt);
        caloriesBurnt.setText("Calories burned : "+totalCalories);

        oldLocation= new Location("dummy data");

        buildGoogleApiClient();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(false)
                .add(   new LatLng(34.065909, -118.168578),
                        new LatLng(34.065827, -118.168613),
                        new LatLng(34.065676, -118.168688),
                        new LatLng(34.065525, -118.168720),
                        new LatLng(34.065502, -118.168758),
                        new LatLng(34.065432, -118.168693),
                        new LatLng(34.065339,-118.168633),
                        new LatLng(34.065262,-118.168515),
                        new LatLng(34.065268,-118.168417),
                        new LatLng(34.065333,-118.168354),
                        new LatLng(34.065395,-118.168386),
                        new LatLng(34.065406,-118.168550))
                .color(Color.BLUE)
                .width(8));

        centerMap();

        myChronometer.setOnChronometerTickListener(
                new Chronometer.OnChronometerTickListener(){

                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        // TODO Auto-generated method stub
                        addToPolyline(oldLocation);
                        calcDist.setText(Math.round(distanceCovered*100.0)/100.0 + " meters");
                    }
                }
        );
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        distanceCovered = distanceCovered + oldLocation.distanceTo(location);
        oldLocation = location;
        centerMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMap();
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.routine){
            Intent intent = new Intent(this, RoutineActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.music) {
            Intent intent = new Intent(this, MusicActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.graph) {
            Intent intent = new Intent(this, GraphActivity.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.profile) {
            Intent intent = new Intent(this,UpdateProfile.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMap() {
        if (mMap == null) {
            return;
        }

        //Request location permission from user so we can pull location
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            if (mRequestingLocationUpdates) {
                createLocationRequest();
                startLocationUpdates();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

         //Gets the most recent location of the device
        if (mLocationPermissionGranted) {
            oldLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            oldLocation = null;
        }

        // Set the map's camera position to the current location of the device.
        // If it can't be found, defaults to GooglePlex
        if (oldLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(oldLocation.getLatitude(),
                            oldLocation.getLongitude()), 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    //Centers the map on the device
    private void centerMap() {
        if (oldLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(oldLocation.getLatitude(),
                            oldLocation.getLongitude()), 15));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void initPolyline() {
        if (line != null) {
            removePolyline();
        }

        PolylineOptions pLineOptions = new PolylineOptions()
                .width(10)
                .color(Color.BLUE);
        line = mMap.addPolyline(pLineOptions);

        routePoints = new ArrayList<>();
        line.setPoints(routePoints);
    }

    //adds coordinates to the arraylist that will form the polyline
    private void addToPolyline(Location location) {
        routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
        line.setPoints(routePoints);
    }

    //clears existing polyline so there is no overlap
    private void removePolyline() {
        line.remove();
        line = null;
    }

    //sorts between start and end tracking
    public void trackBtn(View view) {
        if(startBtn.getText().equals("Start"))
            startTracking(view);
        else
            endTracking(view);
    }

    public void startTracking(View view) {
        startBtn.setText("End");
        initPolyline();
        distanceCovered = 0;
        myChronometer.setBase(SystemClock.elapsedRealtime());
        myChronometer.start();
    }

    public void endTracking(View view) {
        startBtn.setText("Start");
        myChronometer.stop();
    }
}

package com.example.calvin.workout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

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
    private CameraPosition mCameraPosition;
    private static final long LOCATION_REQUEST_INTERVAL = 2000;
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000;
    private CountDownTimer mCDTimer;


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

        calcDist = (TextView)findViewById(R.id.calcDistance);
        calcDist.setText("Put current distance here");
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
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
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
        /*if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }*/
        oldLocation = location;


        centerMap();

        /*if(location != null){
            points.add(new LatLng(location.getLatitude(), location.getLongitude()));
            distance = oldLocation.distanceTo(location);
            oldLocation = location;
            distanceCovered = distanceCovered + distance;
            calcDist.setText("Distance Covered : "+distanceCovered +" meters");
            //calculate calories burnt here
            //caloriesBurnt.setText("Calories burned : "+totalCalories);
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng demo = new LatLng(34.065406,-118.168550);
        points.add(latLng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(demo);
        markerOptions.title("Current Position");
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(demo));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }

    /*public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMap();
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

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
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
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
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
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
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (oldLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(oldLocation.getLatitude(),
                            oldLocation.getLongitude()), 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

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

    private void addToPolyline(Location location) {
        routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
        line.setPoints(routePoints);
    }

    private void removePolyline() {
        line.remove();
        line = null;
    }

    public void trackBtn(View view) {
        if(startBtn.getText().equals("Start"))
            startTracking(view);
        else
            endTracking(view);
    }

    public void startTracking(View view) {
        startBtn.setText("End");
        initPolyline();
        mCDTimer = new CountDownTimer(36000000, 1000) {
            public void onTick(long millisUntilFinished) {
                int mRunTimeSec = (int) ((36000000 - millisUntilFinished) / 1000);

                addToPolyline(oldLocation);
            }

            public void onFinish() {

            }
        };

        mCDTimer.start();
    }

    public void endTracking(View view) {
        startBtn.setText("Start");
        mCDTimer.cancel();
    }
}

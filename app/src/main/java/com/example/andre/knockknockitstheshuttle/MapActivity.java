package com.example.andre.knockknockitstheshuttle;
// Used documentation from https://firebase.google.com/docs/database/android/start/
//https://developers.google.com/maps/documentation/distance-matrix/intro
//https://developers.google.com/maps/documentation/distance-matrix/intro

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;

import java.io.IOException;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    int mapLocation = 0;
    String finalEstimatedTime = "";
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 20 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 secs */

    LatLng latLng;
    List<Address>LocAddress;
    List<Address>myLocationAddressList;
    Address myLocationAddress;
    String origin;
    String destination;
    String StopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Map Entry", "I have entered the onCreate Method");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is TRIGGERED when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be TRIGGERED once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Bundle takes the x variable from Main Activity and sets it to mapLocation to allow it to
        // move the map camera to the right location.
        Bundle bundle = getIntent().getExtras();
        int mapLocation = bundle.getInt("MapMove");
        //Log.d("Map Activity", Integer.toString(mapLocation));
        mMap = googleMap;
        //Log.d("Map Entry", "I have entered the onMapReady Method");

        // Adds the latitude and longitude values of all the shuttle stops. Latitude and longitude
        // values were found at latlong.net
        LatLng Prescott = new LatLng(42.276106, -71.799932);
        mMap.addMarker(new MarkerOptions().position(Prescott).title("85 Prescott Street"));
        LatLng Bartlett = new LatLng(42.273727, -71.808906);
        mMap.addMarker(new MarkerOptions().position(Bartlett).title("Bartlett Center"));
        LatLng GatewayPark = new LatLng(42.275485, -71.799031);
        mMap.addMarker(new MarkerOptions().position(GatewayPark).title("Gateway Park"));
        LatLng Salisbury = new LatLng(42.279726, -71.807448);
        mMap.addMarker(new MarkerOptions().position(Salisbury).title("Salisbury Estates"));
        LatLng MainBuilding = new LatLng(42.274575, -71.806272);
        mMap.addMarker(new MarkerOptions().position(MainBuilding).title("WPI Main Facility Building"));
        LatLng FaradayHall = new LatLng(42.275159, -71.801032);
        mMap.addMarker(new MarkerOptions().position(FaradayHall).title("Faraday Hall"));
        LatLng Boynton = new LatLng(42.274380, -71.805367);
        mMap.addMarker(new MarkerOptions().position(Boynton).title("Boynton Street"));

        //Log.d("Map Activity", Integer.toString(mapLocation));

        // Move camera to the right location. The newLatLngZoom also zooms the camera. I found that
        // 17 works well for our purposes but you can change these values.
        if (mapLocation == 1) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Prescott, 17));
            //Log.d("mapActivity", "Calling distance matrix and passing it the lat and long of 85 Prescott St.");
            // Sends Lat and Lng values to Geocoder to get an address.
            destination = "85 Prescott Street, Worcester, MA";
            StopName = "85 Prescott Street";
        } else if (mapLocation == 2) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Bartlett, 17));
            destination = "100 Institute Road, Worcester, MA";
            StopName = "Bartlett Center";
        } else if (mapLocation == 3) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GatewayPark, 17));
            destination = "60 Prescott Street, Worcester, MA";
            StopName = "Gateway Park";
        } else if (mapLocation == 4) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Salisbury, 17));
            destination = "Park Avenue, Worcester, MA";
            StopName = "Salisbury Estates";
        } else if (mapLocation == 5) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainBuilding, 17));
            destination = "37 Lee Street, Worcester, MA";
            StopName = "WPI Main Facility Building";
        } else if (mapLocation == 6) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FaradayHall, 17));
            destination = "10 Faraday Street, Worcester, MA";
            StopName = "Faraday Hall";
        }
        else if (mapLocation == 7)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Boynton, 17));
            destination = "27 Boynton Street, Worcester, MA";
            StopName = "Boynton Street";
        }
        // Requests permission to access current location
        boolean check = checkLocationPermission();
        if (!check) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        startLocationUpdates();
    }

    /**
     * Checks App Location Permissions for GPS
     * @return True if location permissions are enabled. False is not.
     */
    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    // Gets the results of the permissions
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    /**
     * Map button that shows the location of the user
     * @param location Android Location Object, used for its latitude and longitude values
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) {
       /* Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();*/
    }

    /**
     * Map button function that brings refocuses map on user location
     * @return false so that we don't consume the event and the default behavior still occurs
     * (The camera animates to the user's current position)
     */
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Going to your location.", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Calculates the time between two places
     * @param origin the current address of the shuttle
     * @param destination the destination of the shuttle
     */
    public void distanceMatrix(String origin, final String destination) {
        Log.d("mapActivity", "Entered distanceMatrix() method");
        // Defines the API key to use
        String API_KEY = getString(R.string.google_maps_key);
        //Log.d("mapActivity", "API KEY STRING: "+API_KEY);
        //Provides context for the matrix
        GeoApiContext context = new GeoApiContext().setApiKey(API_KEY);

        // If internet permission is not granted, ask for it
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "This app needs internet permission to work", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions((this), new String[]{Manifest.permission.INTERNET}, 1);
        }
        try {
                DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(context);
                //Takes the approved request and turns it into an actual distance matrix using the users current lat and long,
                // and the recipient's address
                //Matrix takes user's origin as a lat and long value, while it takes the recipients location as a street address
                Log.d("mapActivity", "Called request");
                DistanceMatrix distanceMatrix = req.origins(origin).destinations(destination).await();
                //Takes the duration given by distance matrix and writes it to the global variable finalEstimatedTime
                Log.d("mapActivity", "Distance Matrix Created");
                finalEstimatedTime = (distanceMatrix.rows[0].elements[0].duration.humanReadable);
                Log.d("mapActivity", "distanceMatrix functions as desired. Final time: " + finalEstimatedTime);

                addToFirebase();
            }
        catch (Exception e) {
            Log.d("mapActivity", "Catching things if they fail!");
            e.printStackTrace();
        }

    }

    /**
     * Writes a message to the database that sets the values in the database to the times
     * of each respective shuttle stop
     */
    private void addToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Shuttle Stops");
        if(destination.equals("85 Prescott Street, Worcester, MA")) {
            myRef.child("Loc1").setValue(StopName+": "+finalEstimatedTime);
        }
        if(destination.equals("100 Institute Road, Worcester, MA")) {
            myRef.child("Loc2").setValue(StopName+": "+finalEstimatedTime);
        }
        if(destination.equals("60 Prescott Street, Worcester, MA")) {
            myRef.child("Loc3").setValue(StopName+": "+finalEstimatedTime);
        }
        if(destination.equals("Park Avenue, Worcester, MA")) {
            myRef.child("Loc4").setValue(StopName+": "+finalEstimatedTime);
        }
        if(destination.equals("37 Lee Street, Worcester, MA")) {
            myRef.child("Loc5").setValue(StopName+": "+finalEstimatedTime);
        }
        if(destination.equals("10 Faraday Street, Worcester, MA")) {
            myRef.child("Loc6").setValue(StopName+": "+finalEstimatedTime);
        }
        if(destination.equals("27 Boynton Street, Worcester, MA")) {
            myRef.child("Loc7").setValue(StopName+": "+finalEstimatedTime);
        }
    }

    /**
     * Trigger new location updates at an interval defined by Update_Interval and Fastest_Interval
     */
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        boolean check = checkLocationPermission();
        if (!check) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    /**
     * Method that runs when the location is changed
     * onLcoationChanged geocodes location into an address that the distanceMatrix method uses
     * onLocationChanged also accesses the distanceMatrix method
     * @param location Android location object that contains longitude and latitude values
     */
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        //Send the new LatLng Value to Geocoder to convert to Address
        myLocationAddressList = getAddress(location);
        myLocationAddress = myLocationAddressList.get(0);
        origin = parseAddress(myLocationAddress);
        Log.d("mapActivity", "Origin Address: "+origin);
        Thread distThread = new Thread(new distanceThread());
        distThread.start();
        Log.d("mapActivity", "Final Estimated Time: " +finalEstimatedTime);
        if(!finalEstimatedTime.equals("")) {
           /* Toast.makeText(this, "Time until " + destination + ": " + finalEstimatedTime, Toast.LENGTH_SHORT).show();;*/
        }
    }

    /**
     * Gets last lccation known by the device
     */
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        boolean check = checkLocationPermission();
        if (!check) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    // Create a latlng value if getLastLocation Method is successful
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            latLng= new LatLng(location.getLatitude(), location.getLongitude());
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    // Occurs if getLastLocation is unsuccessful
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("mapActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Gets the address from the given lat and long coordinates to be used in the distance matrix
     * @param location Android location object that contains longitude and latitude values
     * @return Geocoded location address.
     * @return null if IOException
     */
    //gets the address from the given lat and long coordinates to be used in the distance matrix
    public List getAddress(Location location){
        Geocoder geocode = new Geocoder(this);
        try {
            LocAddress = geocode.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            //Toast.makeText(this, LocAddress.get(0).toString(), Toast.LENGTH_SHORT).show();
            return LocAddress;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("mapActivity", "getAddress died :<");
            return null;
        }
    }

    /**
     * Address Parser Class
     * @param Address Geocoded Address
     * @return a string that has a detailed address that the distance matrix uses
     */
    public String parseAddress(Address Address){
        String addressString;
        addressString = Address.getSubThoroughfare();
        addressString += " ";
        addressString += Address.getThoroughfare();
        addressString += " ";
        addressString += Address.getLocality();
        addressString += " ";
        addressString += Address.getAdminArea();
        return addressString;
    }

    /**
     * Second thread to deal with distanceMatrix requests
     */
    private class distanceThread implements Runnable{
        @Override
        public void run()
        {
            distanceMatrix(origin,destination);
        }
    }
}

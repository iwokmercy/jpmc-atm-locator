package com.example.mjexco.jpmcatmlocator;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.example.mjexco.jpmcatmlocator.service.request.AtmLocationService;
import com.example.mjexco.jpmcatmlocator.util.UserLocationProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.loopj.android.http.RequestParams;


public class AtmLocationsActivity extends FragmentActivity implements OnMapReadyCallback, UserLocationProvider.UserLocationCallback {

    private GoogleMap mMap;
    private UserLocationProvider locationProvider;
    private com.example.mjexco.jpmcatmlocator.domain.Location selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atm_locations);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Instantiate UserLocationProvider class needed to get user's current location
        locationProvider = new UserLocationProvider(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //connect google api client
        locationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //disconnect google api client
        locationProvider.disconnect();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(AtmLocationsActivity.this, AtmLocationDetailsActivity.class);
                int locationIndex = Integer.parseInt(marker.getTitle());
                selectedLocation = AtmLocationService.getInstance().getLocations().get(locationIndex);
                intent.putExtra("AtmDetails", selectedLocation);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //app has required permissions so go ahead and get user location
            locationProvider.getUserLocation();
        } else {
            //app permissions denied, throw error and close app
            new AlertDialog.Builder(AtmLocationsActivity.this)
                    .setTitle(getString(R.string.loc_err_title))
                    .setMessage(getString(R.string.loc_err_msg))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    /**
     * This method takes the user's current location, breaks it up into longitude and latitude parameters
     * which it then passes on to the AtmLocationService class along with service url, map surface and activity context.
     * This method is called every time the user's location is updated.
     * @param location user's current location
     */
    public void handleNewLocation(Location location) {
        double currentUserLatitude = location.getLatitude();
        double currentUserLongitude = location.getLongitude();

        //move camera to general user location
        LatLng userLocation = new LatLng(currentUserLatitude, currentUserLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        params.put("lat", currentUserLatitude);
        params.put("lng", currentUserLongitude);

        // make service call to retrieve atm locations
        AtmLocationService.getInstance().makeAtmLocationServiceCall(this, getString(R.string.serv_url), params, mMap);

    }
}

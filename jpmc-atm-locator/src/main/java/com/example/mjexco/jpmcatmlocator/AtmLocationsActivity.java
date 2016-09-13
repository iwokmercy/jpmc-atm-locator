package com.example.mjexco.jpmcatmlocator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.example.mjexco.jpmcatmlocator.service.response.AtmLocationsServiceResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;


public class AtmLocationsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentUserLocation;
    private LocationRequest mLocationRequest;
    AtmLocationsServiceResponse json;
    private com.example.mjexco.jpmcatmlocator.domain.Location selectedLocation;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atm_locations);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(600000)        // 10 minutes, in milliseconds
                .setFastestInterval(60000); // 1 minute, in milliseconds
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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
                selectedLocation = json.getLocations().get(locationIndex);
                intent.putExtra("AtmDetails", selectedLocation);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET}, 1);
            return;
        }
        mCurrentUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentUserLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(mCurrentUserLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //ignore error here as this is only done if permission has been granted by user
            mCurrentUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mGoogleApiClient.isConnected()) {
                if (mCurrentUserLocation == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                } else {
                    handleNewLocation(mCurrentUserLocation);
                }
            }
        } else {
            new AlertDialog.Builder(AtmLocationsActivity.this)
                    .setTitle("Location Error")
                    .setMessage("Location permission denied, you cannot use this app")
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            //Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    private void handleNewLocation(Location location) {
        double currentUserLatitude = location.getLatitude();
        double currentUserLongitude = location.getLongitude();

        //move camera to general user location
        LatLng userLocation = new LatLng(currentUserLatitude, currentUserLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        String url = "https://m.chase.com/PSRWeb/location/list.action";
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        params.put("lat", currentUserLatitude);
        params.put("lng", currentUserLongitude);

        // make service call to retrieve atm locations
        atmLocationServiceCall(url, params);

    }

    private void atmLocationServiceCall(String url, RequestParams params) {
        final ProgressDialog mDialog = new ProgressDialog(AtmLocationsActivity.this);
        mDialog.setMessage("Please wait..");
        mDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody, StandardCharsets.UTF_8);
                Gson gson = new Gson();
                json = gson.fromJson(response, AtmLocationsServiceResponse.class);
                final LatLngBounds.Builder builder = new LatLngBounds.Builder();

                if(!json.hasErrors()) {
                    for (int i = 0; i < json.getLocations().size(); i++) {
                        com.example.mjexco.jpmcatmlocator.domain.Location location = json.getLocations().get(i);
                        double currentLatitude = Double.parseDouble(location.getLat());
                        double currentLongitude = Double.parseDouble(location.getLng());
                        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

                        MarkerOptions options = new MarkerOptions()
                                .position(latLng)
                                .title("" + i)
                                .snippet(location.getName());
                        mMap.addMarker(options);
                        builder.include(latLng);
                    }
                    LatLngBounds bounds = builder.build();
                    final CameraUpdate cu = CameraUpdateFactory.zoomTo(12.0f);
                    mMap.animateCamera(cu);
                }
                else{
                    new AlertDialog.Builder(AtmLocationsActivity.this)
                            .setTitle("Location Error")
                            .setMessage(json.getErrors().get(0).getMessage())
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                mDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}

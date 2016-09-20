package com.example.mjexco.jpmcatmlocator.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class UserLocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public abstract interface UserLocationCallback{
        public void handleNewLocation(Location location);
    }

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private UserLocationCallback mUserLocationCallback;
    private Location mCurrentUserLocation;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 3;

    public UserLocationProvider(Context context, UserLocationCallback callback){
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(600000)        // 10 minutes, in milliseconds
                .setFastestInterval(60000); // 1 minute, in milliseconds

        mUserLocationCallback = callback;
        mContext = context;
    }

    /**
     * Method to connect Google Api Client
     */
    public void connect(){
        mGoogleApiClient.connect();
    }

    /**
     * Method to disconnect Google Api Client
     */
    public void disconnect(){
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Check if the app has required permissions
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request required permissions before continuing
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET}, 1);
            return;
        }
        //app has required permissions so go ahead and get user location
       getUserLocation();
    }

    /**
     * Method to get current user location once Google Api Client is connected
     */
    public void getUserLocation(){
        //ignore warning below as this is called only if permission is granted
        mCurrentUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mGoogleApiClient.isConnected()) {
            if (mCurrentUserLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                mUserLocationCallback.handleNewLocation(mCurrentUserLocation);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult((Activity)mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mUserLocationCallback.handleNewLocation(location);
    }

}

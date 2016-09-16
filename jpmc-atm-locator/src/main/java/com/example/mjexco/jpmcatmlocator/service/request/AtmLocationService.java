package com.example.mjexco.jpmcatmlocator.service.request;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.mjexco.jpmcatmlocator.domain.Location;
import com.example.mjexco.jpmcatmlocator.service.response.AtmLocationsServiceResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Singleton Class to make the service call to retrieve the atm locations
 * then place them on the map.
 */
public class AtmLocationService {
    private static AtmLocationService mInstance;
    private List<Location> locations;

    private AtmLocationService(){
        mInstance = null;
        locations = new ArrayList<>();
    }

    /**
     * Returns an instance of the AtmLocationService class
     * @return
     */
    public static AtmLocationService getInstance(){
        if(mInstance == null) {
            mInstance = new AtmLocationService();
        }
        return mInstance;
    }

    /**
     * Returns the ATM locations gotten in the service response
     * @return
     */
    public List<Location> getLocations(){
        return locations;
    }

    /**
     * Method to make the service call to retrieve atm locations given the user's current location as
     * longitude and latitude parameters
     * @param context references the calling activity
     * @param url service url to get response from
     * @param requestParams service request parameters
     * @param map map surface where atm location markers will be placed
     */
    public void makeAtmLocationServiceCall(final Context context, String url, RequestParams requestParams, final GoogleMap map){
        final ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setMessage("Please wait..");
        mDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody, StandardCharsets.UTF_8);
                Gson gson = new Gson();
                AtmLocationsServiceResponse json = gson.fromJson(response, AtmLocationsServiceResponse.class);
                final LatLngBounds.Builder builder = new LatLngBounds.Builder();

                if(!json.hasErrors()) {
                    locations = json.getLocations();
                    for (int i = 0; i < locations.size(); i++) {
                        com.example.mjexco.jpmcatmlocator.domain.Location location = json.getLocations().get(i);
                        double currentLatitude = Double.parseDouble(location.getLat());
                        double currentLongitude = Double.parseDouble(location.getLng());
                        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

                        MarkerOptions options = new MarkerOptions()
                                .position(latLng)
                                .title("" + i)
                                .snippet(location.getName());
                        map.addMarker(options);
                        builder.include(latLng);
                    }
                    LatLngBounds bounds = builder.build();
                    final CameraUpdate cu = CameraUpdateFactory.zoomTo(12.0f);
                    map.animateCamera(cu);
                }
                else{
                    new AlertDialog.Builder(context)
                            .setTitle("Location Error")
                            .setMessage(json.getErrors().get(0).getMessage())
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = (Activity) context;
                                    dialog.dismiss();
                                    activity.finish();
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
}

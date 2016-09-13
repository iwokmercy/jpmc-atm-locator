package com.example.mjexco.jpmcatmlocator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mjexco.jpmcatmlocator.domain.Location;

import java.util.List;

public class AtmLocationDetailsActivity extends AppCompatActivity {

    private Location selectedLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atm_location_details);

        Intent intent = getIntent();
        selectedLocation = (Location) intent.getSerializableExtra("AtmDetails");
        Log.d("Location", selectedLocation.toString());

        getSupportActionBar().setTitle("ATM (" + selectedLocation.getName() + ")");

        populateAddressSection();
    }

    private void populateAddressSection() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atm_info_details);
        ViewGroup parentGroup = parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int i=0; i<6; i++){
            if(i==0){
                //address item with onclick
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setImageDrawable(getResources().getDrawable(R.drawable.marker));
                entryImage.setVisibility(View.VISIBLE);
                entryText.setText(constructAddress());

                LinearLayout addressLayout = (LinearLayout) view.findViewById(R.id.detail_item);
                addressLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // launch map app with address pre-populated
                        String url = "geo:0,0?q=" + constructAddress();
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
                        // Verify that there are applications registered to handle this intent
                        // (resolveActivity returns null if none are registered)
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }else{
                            Toast.makeText(AtmLocationDetailsActivity.this, "You do not have a GPS app on your phone", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                parentGroup.addView(view);
            } else if(i==1){
                //bank item no onclick
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText("Bank: " + selectedLocation.getBank());

                parentGroup.addView(view);
            }else if(i==2){
                //phone item wth onclick
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setImageDrawable(getResources().getDrawable(R.drawable.phone));
                entryImage.setVisibility(View.VISIBLE);
                entryText.setText(selectedLocation.getPhone());

                LinearLayout phoneLayout = (LinearLayout) view.findViewById(R.id.detail_item);
                phoneLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // launch phone app with number pre-populated
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", selectedLocation.getPhone(), null));
                        // Verify that there are applications registered to handle this intent
                        // (resolveActivity returns null if none are registered)
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }else{
                            Toast.makeText(AtmLocationDetailsActivity.this, "You do not have an app for making calls on your phone", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                parentGroup.addView(view);
            }else if(i==3){
                //distance item no onclick
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText("Distance: " + selectedLocation.getDistance());

                parentGroup.addView(view);
            }else if(i==4){
                //access item no onclick
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText("Access: " + selectedLocation.getAccess());

                parentGroup.addView(view);
            }else{
                //type item no onclick
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText("Type: " + selectedLocation.getType());

                parentGroup.addView(view);
            }
        }
        populateServiceDetails();
    }

    private void populateServiceDetails() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atm_service_details);
        ViewGroup parentGroup = parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(selectedLocation.hasServices()){
            List<String> services = selectedLocation.getServices();
            TextView label = (TextView) findViewById(R.id.services_label);
            label.setVisibility(View.VISIBLE);
            for(int i=0; i<services.size(); i++){
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText("" + services.get(i));

                parentGroup.addView(view);
            }
        }else{
            View divider = findViewById(R.id.services_divider);
            divider.setVisibility(View.GONE);
        }

        populateLobbyHoursDetails();
    }

    private void populateLobbyHoursDetails() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atmlb_hrs_details);
        ViewGroup parentGroup = parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(selectedLocation.hasLobbyHrs()){
            List<String> lobbyHrs = selectedLocation.getLobbyHrs();
            TextView label = (TextView) findViewById(R.id.lobby_label);
            label.setVisibility(View.VISIBLE);
            for(int i=0; i<lobbyHrs.size(); i++){
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText(getDay(i) + lobbyHrs.get(i));

                parentGroup.addView(view);
            }
        }else{
            View divider = findViewById(R.id.services_divider);
            divider.setVisibility(View.GONE);
        }

        populateDriveUpHours();
    }

    private void populateDriveUpHours() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atmdu_hrs_details);
        ViewGroup parentGroup = parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(selectedLocation.hasDriveUpHrs()){
            List<String> driveUpHrs = selectedLocation.getDriveUpHrs();
            TextView label = (TextView) findViewById(R.id.driveup_label);
            label.setVisibility(View.VISIBLE);
            for(int i=0; i<driveUpHrs.size(); i++){
                View view = inflater.inflate(R.layout.atm_details_item, null, true);
                ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
                TextView entryText = (TextView) view.findViewById(R.id.entry_text);
                entryImage.setVisibility(View.INVISIBLE);
                entryText.setText(getDay(i) + driveUpHrs.get(i));

                parentGroup.addView(view);
            }
        }else{
            View divider = findViewById(R.id.services_divider);
            divider.setVisibility(View.GONE);
        }
    }

    private String getDay(int i) {
        return Days.values()[i].toString() + ":     ";
    }

    private String constructAddress() {
        return selectedLocation.getAddress() + ", " + selectedLocation.getCity() + ", " + selectedLocation.getState()
                + ", " + selectedLocation.getZip();
    }

    public void performAction(View view) {
        //do nothing
    }

    enum Days {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;

        @Override
        public String toString() {
            // only capitalize the first letter
            String s = super.toString();
            return s.substring(0, 1) + s.substring(1).toLowerCase();
        }
    }
}

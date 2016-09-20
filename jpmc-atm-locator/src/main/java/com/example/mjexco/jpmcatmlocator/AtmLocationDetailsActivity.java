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

        /**
         * Differentiate between bank branches and stand-alone ATMs
         */
        if(selectedLocation.getLocType().equals("branch")){
            getSupportActionBar().setTitle("BRANCH (" + selectedLocation.getName() + ")");
        } else {
            getSupportActionBar().setTitle("ATM (" + selectedLocation.getName() + ")");
        }

        populateAddressSection();
    }

    /**
     * Method to inject views into the address section of the details page
     */
    private void populateAddressSection() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atm_info_details);
        ViewGroup parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int i=0; i<6; i++){
            if(i==0){
                parentGroup.addView(createMediaItem(inflater, 0));
            } else if(i==1){
                parentGroup.addView(createTextItem(inflater, getString(R.string.bank_txt) + selectedLocation.getBank()));
            }else if(i==2){
                parentGroup.addView(createMediaItem(inflater, 1));
            }else if(i==3){
                parentGroup.addView(createTextItem(inflater, getString(R.string.dist_txt) + selectedLocation.getDistance()));
            }else if(i==4){
                parentGroup.addView(createTextItem(inflater, getString(R.string.access_txt) + selectedLocation.getAccess()));
            }else{
                parentGroup.addView(createTextItem(inflater, getString(R.string.type_txt) + selectedLocation.getType()));
            }
        }
        populateServiceDetails();
    }

    /**
     * Method to create a text detail item view to be injected into the layout
     */
    private View createTextItem(LayoutInflater inflater, String text){
        View view = inflater.inflate(R.layout.atm_details_item, null, true);
        ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
        TextView entryText = (TextView) view.findViewById(R.id.entry_text);
        entryImage.setVisibility(View.INVISIBLE);
        entryText.setText(text);
        return view;
    }

    /**
     * Method to create a media detail item view to be injected into the layout
     * This item has two types; GPS Item with ID 0, Phone Item with ID 1
     * Each item has it's on OnClickListener; the GPS item OnClickListener launches GPS app on
     * the device with ATM address pre-populated. Phone item onClickListener launches phone app
     * with ATM phone number pre-populated.
     */
    private View createMediaItem(LayoutInflater inflater, int type){
        View view = inflater.inflate(R.layout.atm_details_item, null, true);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.detail_item);
        ImageView entryImage = (ImageView) view.findViewById(R.id.entry_image);
        TextView entryText = (TextView) view.findViewById(R.id.entry_text);

        View.OnClickListener addressOnClick = new View.OnClickListener() {
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
                    Toast.makeText(AtmLocationDetailsActivity.this, R.string.gps_app_err, Toast.LENGTH_LONG).show();
                }
            }
        };

        View.OnClickListener phoneOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launch phone app with number pre-populated
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", selectedLocation.getPhone(), null));
                // Verify that there are applications registered to handle this intent
                // (resolveActivity returns null if none are registered)
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }else{
                    Toast.makeText(AtmLocationDetailsActivity.this, R.string.phone_app_err, Toast.LENGTH_LONG).show();
                }
            }
        };

        if(type == 0){
            //address type
            entryImage.setImageDrawable(getResources().getDrawable(R.drawable.marker));
            entryImage.setVisibility(View.VISIBLE);
            entryText.setText(selectedLocation.getAddress());
            layout.setOnClickListener(addressOnClick);
        } else{
            //phone type
            entryImage.setImageDrawable(getResources().getDrawable(R.drawable.phone));
            entryImage.setVisibility(View.VISIBLE);
            entryText.setText(selectedLocation.getPhone());
            layout.setOnClickListener(phoneOnClick);
        }

        return view;
    }

    /**
     * Method to inject views into the service section of the details page layout
     */
    private void populateServiceDetails() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atm_service_details);
        ViewGroup parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(selectedLocation.hasServices()){
            List<String> services = selectedLocation.getServices();
            TextView label = (TextView) findViewById(R.id.services_label);
            label.setVisibility(View.VISIBLE);
            for(int i=0; i<services.size(); i++){
                parentGroup.addView(createTextItem(inflater, "" + services.get(i)));
            }
        }else{
            View divider = findViewById(R.id.services_divider);
            divider.setVisibility(View.GONE);
        }

        populateLobbyHoursDetails();
    }

    /**
     * Method to inject views into the lobby hours section of the details page layout
     */
    private void populateLobbyHoursDetails() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atmlb_hrs_details);
        ViewGroup parentGroup = (ViewGroup) addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(selectedLocation.hasLobbyHrs()){
            List<String> lobbyHrs = selectedLocation.getLobbyHrs();
            TextView label = (TextView) findViewById(R.id.lobby_label);
            label.setVisibility(View.VISIBLE);
            for(int i=0; i<lobbyHrs.size(); i++){
                parentGroup.addView(createTextItem(inflater, getDay(i) + lobbyHrs.get(i)));
            }
        }else{
            View divider = findViewById(R.id.services_divider);
            divider.setVisibility(View.GONE);
        }

        populateDriveUpHours();
    }

    /**
     * Method to inject views into the drive-up hours section of the details page layout
     */
    private void populateDriveUpHours() {
        LinearLayout addressSectionLayout = (LinearLayout)findViewById(R.id.atmdu_hrs_details);
        ViewGroup parentGroup = (ViewGroup)addressSectionLayout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(selectedLocation.hasDriveUpHrs()){
            List<String> driveUpHrs = selectedLocation.getDriveUpHrs();
            TextView label = (TextView) findViewById(R.id.driveup_label);
            label.setVisibility(View.VISIBLE);
            for(int i=0; i<driveUpHrs.size(); i++){
                parentGroup.addView(createTextItem(inflater, getDay(i) + driveUpHrs.get(i)));
            }
        }else{
            View divider = findViewById(R.id.services_divider);
            divider.setVisibility(View.GONE);
        }
    }

    /**
     * Method to retrieve day values from the Days enum
     */
    private String getDay(int i) {
        return Days.values()[i].toString() + ":     ";
    }

    /**
     * Method to construct ATM address
     */
    private String constructAddress() {
        return selectedLocation.getAddress() + ", " + selectedLocation.getCity() + ", " + selectedLocation.getState()
                + ", " + selectedLocation.getZip();
    }

    public void performAction(View view) {
        //do nothing
    }

    /**
     * Enum with days of the week
     * Returned day is formatted with only the first letter capitalized
     */
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

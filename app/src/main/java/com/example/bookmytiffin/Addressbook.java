package com.example.bookmytiffin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapmyindia.sdk.plugins.places.placepicker.PlacePicker;
import com.mapmyindia.sdk.plugins.places.placepicker.model.PlacePickerOptions;
import com.mmi.services.api.Place;

import java.util.List;
import java.util.Locale;


public class Addressbook extends AppCompatActivity implements LocationListener {

    String origin;



    LocationManager locationManager;

    DatabaseReference addressreference;

    Boolean editaddress1 = false, editaddress2 = false;

    FirebaseAuth firebaseAuth;

    EditText yourlocation;

    Button saveaddress;

    String extraaddress;

    TextView addresstext1, addresstext2, addaddress;

    ImageView addaddressimage, more1, more2;

    int countaddress = 0;

    Button nextaddress;

    private final int REQUEST_CHECK_CODE = 8989;

    private LocationSettingsRequest.Builder builder;

    private String provider = LocationManager.NETWORK_PROVIDER;

    String liveaddress;

    Double livelat, livelong;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addressbook);

        origin = getIntent().getStringExtra("origin");

        nextaddress = findViewById(R.id.nextaddressbutton);

        if(origin.equals("profile")){
            nextaddress.setVisibility(View.GONE);
        }




        more1 = findViewById(R.id.more);
        more2 = findViewById(R.id.more2);




        addresstext1 = findViewById(R.id.addresstext1);

        addresstext2 = findViewById(R.id.addresstext2);

        addaddress = findViewById(R.id.addaddress);

        addaddressimage = findViewById(R.id.addaddressimage);

        if(countaddress == 0){

            nextaddress.setClickable(false);
            nextaddress.setEnabled(false);
            more1.setVisibility(View.GONE);
            more2.setVisibility(View.GONE);
            addresstext1.setVisibility(View.GONE);
            addresstext2.setVisibility(View.GONE);
        }
        else if(countaddress == 1){
            more2.setVisibility(View.GONE);
            addresstext2.setVisibility(View.GONE);
        }
        else if(countaddress == 2)
        {
            addaddressimage.setVisibility(View.GONE);
            addaddress.setVisibility(View.GONE);
        }

        LocationRequest request =  LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);

        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);


        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(Addressbook.this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    System.out.println("Response"+response.toString());
                    Toast.makeText(Addressbook.this, "Gps already enabled", Toast.LENGTH_SHORT).show();
                    provider = LocationManager.GPS_PROVIDER;
                    getLocation();

                } catch (ApiException e) {
                    switch ( e.getStatusCode())
                    {
                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(Addressbook.this, REQUEST_CHECK_CODE );

                            }
                            catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }catch (ClassCastException ex){

                            }break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        {
                            break;
                        }

                    }
                }

            }
        });



        addaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomsheet();

            }
        });


        addaddressimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomsheet();

            }
        });


        more2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String [] tempaddress = addresstext2.getText().toString().split(":");
                liveaddress = tempaddress[1];
                extraaddress = tempaddress[0];
                editaddress2 = true;

                bottomsheet();
            }
        });

        more1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String [] tempaddress = addresstext1.getText().toString().split(":");
                liveaddress = tempaddress[1];
                extraaddress = tempaddress[0];
                editaddress1 = true;
                //livelat =
                bottomsheet();
            }
        });


        nextaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Addressbook.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == REQUEST_CHECK_CODE) {

             if(resultCode == RESULT_OK)
             {
                 Toast.makeText(this, "GPS Enabled by user", Toast.LENGTH_SHORT).show();
                 provider = LocationManager.GPS_PROVIDER;
                 getLocation();
             }
             else if (resultCode == RESULT_CANCELED){
                 provider = LocationManager.NETWORK_PROVIDER;
                 Toast.makeText(this, "GPS Cancelled by user", Toast.LENGTH_SHORT).show();
             }
         }

        else if (requestCode == 101 && resultCode == Activity.RESULT_OK) {

            Place place = PlacePicker.getPlace(data);
            if(place!= null || place.getFormattedAddress()!= null || place.getLat()!= null || place.getLng()!= null){
                liveaddress = place.getFormattedAddress();

                livelat = Double.parseDouble(place.getLat());

                livelong = Double.parseDouble(place.getLng());

                yourlocation.setText(liveaddress);

                //System.out.println("lat" + place.getLat() + " long" +place.getLng());

            }else {
                Toast.makeText(this, "Something went wrong, not able to fetch your location", Toast.LENGTH_SHORT).show();
            }

        }

    }


    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(provider, 100, 5, (LocationListener)this);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);



            liveaddress = addresses.get(0).getAddressLine(0);
            livelat = location.getLatitude();
            livelong = location.getLatitude();


            locationManager.removeUpdates((LocationListener)this);
            //locationfetch=1;


        }
        catch (Exception e) {
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }






    public void bottomsheet() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Addressbook.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_address);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();





        yourlocation = bottomSheetDialog.findViewById(R.id.yourlocation1);

        saveaddress = bottomSheetDialog.findViewById(R.id.addaddressbutton);

        EditText exactaddress = bottomSheetDialog.findViewById(R.id.completeaddress1);

        EditText landmark = bottomSheetDialog.findViewById(R.id.landmark1);

        if(editaddress1 || editaddress2) {
            exactaddress.setText(extraaddress);
        }


        yourlocation.setText(liveaddress);





        yourlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Current_Location curr_loc = (Current_Location) getApplication();

                Intent i = new PlacePicker.IntentBuilder()
                        .placeOptions(PlacePickerOptions.builder()
                                .statingCameraPosition(new CameraPosition.Builder()
                                        .target(new LatLng(curr_loc.getCurr_lat(), curr_loc.getCurr_long())).zoom(16).build())
                                .build()).build(Addressbook.this);
                startActivityForResult(i, 101);

            }
        });


        saveaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(exactaddress.getText())){
                    Toast.makeText(Addressbook.this, "Please fill the Wing, House Number and Building Name", Toast.LENGTH_SHORT).show();
                }
                String fulladdress = exactaddress.getText().toString()+"; "+landmark.getText().toString()+ ": " + yourlocation.getText().toString();
                firebaseAuth = FirebaseAuth.getInstance();
                addressreference = FirebaseDatabase.getInstance().getReference("users").child(firebaseAuth.getUid());

                 com.example.bookmytiffin.Address newaddress = new com.example.bookmytiffin.Address(fulladdress, livelat, livelong);

                 if (!editaddress1 && !editaddress2)
                 {
                     countaddress++;
                 }

                if(editaddress1 ||countaddress == 1){


                    addresstext1.setVisibility(View.VISIBLE);
                    more1.setVisibility(View.VISIBLE);
                    addresstext1.setText(fulladdress);
                    addressreference.child("address1").setValue(newaddress);
                    editaddress1 = false;
                }

                else if(editaddress2 ||countaddress == 2){
                    addresstext2.setVisibility(View.VISIBLE);
                    more2.setVisibility(View.VISIBLE);
                    addresstext2.setText(fulladdress);
                    addressreference.child("address2").setValue(newaddress);
                    editaddress2 = false;
                }

                nextaddress.setEnabled(true);
                nextaddress.setClickable(true);




            }
        });

    }
}
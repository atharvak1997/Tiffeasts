package com.example.bookmytiffin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shakebugs.shake.Shake;

import java.util.List;
import java.util.Locale;

public class Splash extends AppCompatActivity implements LocationListener{
    private static int SPLASH_TIME_OUT = 2000;
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    static Userinfo curruser;
    int userfetch=0,locationfetch=0;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Shake.start(getApplication());
        setContentView(R.layout.activity_splash);

        SharedPreferences userdatastore = getSharedPreferences("userdatastore", Context.MODE_PRIVATE);
        String username = userdatastore.getString("name","");

        if(FirebaseAuth.getInstance().getCurrentUser() != null && username.equals("")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    curruser = dataSnapshot.getValue(Userinfo.class);
                    userfetch = 1;
                    SharedPreferences.Editor editor = userdatastore.edit();
                    editor.putString("name",curruser.getName());
                    editor.putString("email",curruser.getEmail());
                    editor.putString("mobileno",curruser.getMobileno());
                    editor.putInt("rating_count",curruser.getRating_count());
                    editor.putInt("rating_sum",curruser.getRating_sum());
                    editor.putInt("verified",curruser.getVerified());
                    editor.apply();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        else if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            username = userdatastore.getString("name","");
            String email = userdatastore.getString("email","");
            String mobileno = userdatastore.getString("mobileno","");
            int rating_count = userdatastore.getInt("rating_count",0);
            int rating_sum = userdatastore.getInt("rating_sum",0);
            int verified = userdatastore.getInt("verified",0);
            curruser = new Userinfo(username,email,mobileno,rating_count,rating_sum,verified);
            userfetch=1;
        }


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            getlastlocation();

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                timer = new CountDownTimer(5000, 300) {

                    public void onTick(long millisUntilFinished) {
                        nextactivity();
                    }

                    public void onFinish() {
                        if (userfetch == 0)
                            Toast.makeText(getApplicationContext(), "Unable to load your profile\nCheck your internet connection and open app again", Toast.LENGTH_LONG).show();
                        else if (locationfetch == 0)
                            Toast.makeText(getApplicationContext(), "Unable to detect your live location\nUsing default location", Toast.LENGTH_LONG).show();
                        locationfetch = 1;
                        nextactivity();
                    }
                }.start();
            }
            else
            {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(Splash.this, SendOTP.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                },2000);

            }
        }

    }

    void nextactivity()
    {
        if(locationfetch==1 && userfetch==1) {
            timer.cancel();
            if (curruser != null) {
                Intent i = new Intent(Splash.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
            else{
                Intent i = new Intent(Splash.this, Userdetails.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == 101) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    recreate();
                }
            }
            else {
                boolean showRationale1 = shouldShowRequestPermissionRationale(permissions[0]);
                boolean showRationale2 = shouldShowRequestPermissionRationale(permissions[1]);
                if (!showRationale1 || !showRationale2) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Disabled")
                            .setMessage("We need your location to show food items around you. You have permanently disabled permissions for the app.\nEnable permissions from app setting. If permission is not granted the app will be closed.")
                            .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 102);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    System.exit(0);
                                }
                            }).create().show();

                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Required")
                            .setMessage("We need your location to show food items around you. If permission is not granted the app will be closed.\nAre you sure you want to deny this permission?")
                            .setNegativeButton("RE-TRY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(Splash.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
                                }
                            })
                            .setPositiveButton("I'M SURE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    System.exit(0);
                                }
                            }).create().show();
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    void getlastlocation()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                Current_Location loc = (Current_Location) getApplication();
                                loc.setCurr_lat(location.getLatitude());
                                loc.setCurr_long(location.getLongitude());
                                loc.setCurr_address(addresses.get(0).getAddressLine(0));
                                locationfetch=1;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            getLocation();
                        }

                    }
                });

    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 5, (LocationListener)this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            Current_Location loc = (Current_Location) getApplication();
            loc.setCurr_lat(location.getLatitude());
            loc.setCurr_long(location.getLongitude());
            loc.setCurr_address(addresses.get(0).getAddressLine(0));

            locationManager.removeUpdates((LocationListener)this);
            locationfetch=1;


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

}
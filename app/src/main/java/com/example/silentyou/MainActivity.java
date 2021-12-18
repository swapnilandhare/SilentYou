package com.example.silentyou;

import static android.Manifest.permission.ACCESS_NOTIFICATION_POLICY;
import static android.content.ContentValues.TAG;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Long3;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button addLocation, addGeofence,removeGeofence;
    LatLngList latLngList;
    Geocoder geocoder;

    private GeofenceHelper geofenceHelper;
    private GeofencingClient geofencingClient;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private final float radius = 25;

    ArrayList<LatLng> arrayList;


    private TextView textView, textView3, textView2;
    private int FINE_LOCATION_ACESS_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermission();

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
        textView = findViewById(R.id.textView);
        textView3 = findViewById(R.id.textView3);
        textView2 = findViewById(R.id.textView2);
        addLocation = findViewById(R.id.addLocation);
        addGeofence = findViewById(R.id.addGeofence);
        removeGeofence=findViewById(R.id.removeGeofence);

        geocoder = new Geocoder(this);
        latLngList = new LatLngList(this);

        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        addGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGeofence();
            }
        });
        removeGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
            }
        });
    }

    void addGeofence() {
        arrayList = latLngList.getArrayList();
        if (arrayList==null) {
            Toast.makeText(this, "ArrayList is Empty", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            List<Address> addressList;
            LatLng ltlg = arrayList.get(0);
            Address address;
            addressList = geocoder.getFromLocation(ltlg.latitude, ltlg.longitude, 1);
            address = addressList.get(0);
            String addressLine = address.getAddressLine(0);
            textView.setText(addressLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(arrayList.size()==2)
        {
            try {
                List<Address> addressList;
                LatLng ltlg = arrayList.get(1);
                Address address;
                addressList = geocoder.getFromLocation(ltlg.latitude, ltlg.longitude, 1);
                address = addressList.get(0);
                String addressLine = address.getAddressLine(0);
                textView2.setText(addressLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(arrayList.size()==3)
        {
            try {
                List<Address> addressList;
                LatLng ltlg = arrayList.get(2);
                Address address;
                addressList = geocoder.getFromLocation(ltlg.latitude, ltlg.longitude, 1);
                address = addressList.get(0);
                String addressLine = address.getAddressLine(0);
                textView3.setText(addressLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Add Geofences
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(arrayList);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }

    void setPermission()
    {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }
    }
    void remove()
    {
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.d(TAG, "onSuccess: REMOVED");
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
        textView.setText("");
        textView2.setText("");
        textView3.setText("");
        Toast.makeText(this,"Geofences Removed",Toast.LENGTH_LONG);
    }
}
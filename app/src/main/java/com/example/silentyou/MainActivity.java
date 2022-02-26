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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button addLocation, addGeofence, removeGeofence;
    LatLngList latLngList;
    Geocoder geocoder;

    public static final String MYPREFERENCES = "myPref";

    private GeofenceHelper geofenceHelper;
    private GeofencingClient geofencingClient;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private final float radius = 25;
    private int DELAY = 1500; // Delay time in milliseconds

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
        removeGeofence = findViewById(R.id.removeGeofence);

        geocoder = new Geocoder(this);
        latLngList = new LatLngList(this);

        checkLocations();

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
                arrayList = latLngList.getArrayList();
                if(arrayList != null)
                {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            //TODO your background code
                            addGeofence(arrayList);
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //change UI elements here
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    addText(arrayList);
                                }
                            }, DELAY);
                        }
                    });
                    save(arrayList);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please add some location",Toast.LENGTH_LONG).show();
                }
            }
        });
        removeGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
            }
        });
    }

    void addText(ArrayList<LatLng> arrayList){
        int n=arrayList.size();
        ArrayList<String> addList=new ArrayList<>();
        List<Address> addressList;
        Address address;
        for(LatLng latLng:arrayList)
        {
            try {
                addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                address = addressList.get(0);
                String addressLine = address.getAddressLine(0);
                addList.add(addressLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(n>0)
            textView.setText(addList.get(0));
        if(n>1)
            textView2.setText(addList.get(1));
        if(n>2)
            textView3.setText(addList.get(2));
    }

    void addGeofence(ArrayList<LatLng> arrayList) {

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

    void setPermission() {
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

    void remove() {
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.d(TAG, "onSuccess: REMOVED");
                        Toast.makeText(getApplicationContext(),"ALL GEOFENCES REMOVED",Toast.LENGTH_LONG).show();
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
        SharedPreferences preferences = getSharedPreferences(MYPREFERENCES, 0);
        preferences.edit().clear().commit();
        textView.setText("");
        textView2.setText("");
        textView3.setText("");
        Toast.makeText(this, "Geofences Removed", Toast.LENGTH_LONG);
    }

    void save(ArrayList<LatLng> arrayList) {
        String name = "Obj";
        int i = 1;
        SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        Gson gson = new Gson();
        for (LatLng latLng : arrayList) {
            String json = gson.toJson(latLng);
            myEdit.putString(name + i, json);
            myEdit.commit();
            i++;
        }

}
    void checkLocations()
    {
        ArrayList<LatLng> arrayList=new ArrayList<>();
        String name = "Obj";
        int i = 1;
        SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, MODE_PRIVATE);
        int n=sharedPreferences.getAll().size();
        Gson gson = new Gson();
        for(int j=0;j<n;j++)
        {
            String json = sharedPreferences.getString(name+i, "");
            LatLng obj = gson.fromJson(json, LatLng.class);
            arrayList.add(obj);
            i++;
        }
        if(n>0)
        {
            addText(arrayList);
            addGeofence(arrayList);
        }

    }
}
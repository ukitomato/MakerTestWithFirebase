package com.google.watermap.p.gary.makertestwithfirebase;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {



    private GoogleMap mMap;
    private FragmentActivity fragmentActivity = this;

    private FirebaseDatabase mDatabase;

    private DatabaseReference mDatabaseReference;
    private List<DatabasePlace> dbPlaceList = new ArrayList<>();
    private HashMap<String, DatabasePlace> placeMap = new HashMap<>();


    private LatLng tokyo = new LatLng(35.652832, 139.839478);

    private double earth_dis = 6378137;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference("Places");


        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    putPlaceList(data, dbPlaceList);
                    //putPlaceMap(data, placeMap);
                }
                if (mMap != null) {
                    addMakerAll();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setInfoWindowAdapter(new CustomWindowViewer(fragmentActivity));
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("oMC", "TRUE");
                CameraPosition cameraPosition = mMap.getCameraPosition();
                double theta = cameraPosition.tilt;
                double distance = 1 / earth_dis;
                double moveLat = distance * sin(theta);
                double moveLng = distance * cos(theta);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude + moveLat, marker.getPosition().longitude + moveLng)));
                return true;
            }
        });
        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tokyo));

    }

    public void addMakerAll() {
        for (DatabasePlace dbPlace : dbPlaceList) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(dbPlace.getLocation()).title(dbPlace.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(dbPlace.getMakerColor())));
            marker.setTag(dbPlace);
        }
    }

    private void putPlaceList(DataSnapshot dataSnapshot, List<DatabasePlace> dbPlaceList) {
        String key = dataSnapshot.getKey();
        Log.i("onDataChange", key);
        Object kind = dataSnapshot.child("Kind").getValue();
        Object level = dataSnapshot.child("Level").getValue();
        Object latitude = dataSnapshot.child("Location").child("Latitude").getValue();
        Object longitude = dataSnapshot.child("Location").child("Longitude").getValue();
        Object uri = dataSnapshot.child("ImageURI").getValue();
        Object id = dataSnapshot.child("ID").getValue();
        Log.i("Value", kind + ":" + level + ":" + latitude + ":" + longitude + ":" + id);
        if (latitude != null && longitude != null) {
            DatabasePlace dbPlace = new DatabasePlace(key, (String) kind, (long) level, (Double) latitude, (Double) longitude, (long) id);
            dbPlaceList.add(dbPlace);
            placeMap.put(key, dbPlace);
        }
    }

    private void putPlaceMap(DataSnapshot dataSnapshot, HashMap<String, DatabasePlace> placeMap) {
        String key = dataSnapshot.getKey();
        Log.i("onDataChange", key);
        Object kind = dataSnapshot.child("Kind").getValue();
        Object level = dataSnapshot.child("Level").getValue();
        Object latitude = dataSnapshot.child("Location").child("Latitude").getValue();
        Object longitude = dataSnapshot.child("Location").child("Longitude").getValue();
        Object uri = dataSnapshot.child("ImageURI").getValue();
        Object id = dataSnapshot.child("ID").getValue();
        Log.i("Value", kind + ":" + level + ":" + latitude + ":" + longitude + ":" + uri);
        if (latitude != null && longitude != null) {
            DatabasePlace dbPlace = new DatabasePlace(key, (String) kind, (long) level, (Double) latitude, (Double) longitude, (long) id);

            placeMap.put(key, dbPlace);
        }
    }


}

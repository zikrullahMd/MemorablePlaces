package zik.myappcompany.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleToIntFunction;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    LocationManager locationManager;
    LocationListener locationListener;


    private GoogleMap mMap;

    public void mapOnLocation(Location location, String tittle){
        if(location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();

            mMap.addMarker(new MarkerOptions().position(userLocation).title(tittle));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mapOnLocation(lastKnownLocation,"Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if(intent.getIntExtra("placeNumber",0) == 0){
            locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mapOnLocation(location,"Your Location");
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
            };
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mapOnLocation(lastKnownLocation,"Your Location");
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else{
            Location placelocation = new Location(LocationManager.GPS_PROVIDER);
            placelocation.setAltitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placelocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);
            mapOnLocation(placelocation,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));
        }


    }

    @Override
    public void onMapLongClick(LatLng point) {
        //mMap.addMarker(new MarkerOptions().position(latLng).title("Your New Memorable Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try{
            List<Address> listAddresses = geocoder.getFromLocation(point.latitude,point.longitude,1);
            if(listAddresses != null && listAddresses.size()>0) {

                    if (listAddresses.get(0).getThoroughfare() != null) {
                        if (listAddresses.get(0).getSubThoroughfare() != null){
                            address += listAddresses.get(0).getSubThoroughfare();
                        }



                    }
                    address += listAddresses.get(0).getThoroughfare();
                }

        }catch (Exception E){
            E.printStackTrace();
        }
        if(address.equals("")){
            SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd_HHmmss");
            address += sdf.format(new Date());

        }
        mMap.addMarker(new MarkerOptions().position(point).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        MainActivity.places.add(address);
        MainActivity.locations.add(point);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences = this.getSharedPreferences("zik.myappcompany.memorableplaces",Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();
            for(LatLng coords : MainActivity.locations){
                latitudes.add(Double.toString(coords.latitude));
                longitudes.add(Double.toString(coords.longitude));
            }

            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places));
            sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes));
            sharedPreferences.edit().putString("lons",ObjectSerializer.serialize(longitudes));

        }catch (Exception e){
            e.printStackTrace();
        }

        Toast.makeText(this, "Location added to Memorable Places", Toast.LENGTH_SHORT).show();


    }
}

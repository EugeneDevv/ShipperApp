package com.example.thunderbolt_108.shipperapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thunderbolt_108.shipperapp.Common.Common;
import com.example.thunderbolt_108.shipperapp.Helper.DirectionJSONParser;
import com.example.thunderbolt_108.shipperapp.Model.Request;
import com.example.thunderbolt_108.shipperapp.Remote.IGeoCoordinates;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Location mLastLocation;
    Marker mCurrentMarker;
    IGeoCoordinates mService;
    Polyline polyline;

    ImageView btn_call, btn_shipped;
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mService = Common.getGeoCodeService();

        // Todo Location Already on  ... start
        final LocationManager manager = (LocationManager) TrackingOrder.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(TrackingOrder.this)) {
            Toast.makeText(TrackingOrder.this,"GPS Is Enabled",Toast.LENGTH_SHORT).show();
        }

        // Todo Location Already on  ... end

        if(!hasGPSDevice(TrackingOrder.this)){
            Toast.makeText(TrackingOrder.this,"Gps not Supported",Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(TrackingOrder.this)) {
            Log.e("Shyam","Gps already enable");
            Toast.makeText(TrackingOrder.this,"Please Enable Your GPS",Toast.LENGTH_SHORT).show();
            enableLoc();
        }else{
            Log.e("Shyam","Gps already enabled");
            Toast.makeText(TrackingOrder.this,"GPS Is Enabled",Toast.LENGTH_SHORT).show();
        }

        btn_call = (ImageView) findViewById(R.id.btnCall);
        btn_shipped = (ImageView) findViewById(R.id.btnShipped);

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Common.currentRequest.getPhone()));
                if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivity(intent);
            }
        });

        btn_shipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We will delete order in table
                // -orderNeedShip
                // -shippingOrder
                // And update status of order to shipped
                shippedOrder();
            }
        });

        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void shippedOrder() {

        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_NEED_SHIP_TABLE)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Update status on request table
                        Map<String,Object> update_status=new HashMap<>();
                        update_status.put("status","03");

                        FirebaseDatabase.getInstance()
                                .getReference("Requests")
                                .child(Common.currentKey)
                                .updateChildren(update_status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Delete from shippingOrder
                                        FirebaseDatabase.getInstance()
                                                .getReference(Common.SHIPPER_INFO_TABLE)
                                                .child(Common.currentKey)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(TrackingOrder.this, "Order Shipped Successfully !!!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
                if (mCurrentMarker != null)
                {
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())); //update location for marker

                    //Update Location On Firebase
                    Common.updateShippingInformation(Common.currentKey,mLastLocation);

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
                    drawRoute(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),Common.currentRequest);
                }

            }
        };
    }

    private void drawRoute(final LatLng yourLocation, Request request) {

        //Clear all PolyLine
        if (polyline !=null)
            polyline.remove();

        if (request.getAddress() != null && !request.getAddress().isEmpty())
        {
            mService.getAddressName(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?sensor=false&key=AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY&address=")
                    .append(request.getAddress()).toString())
                    .enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{

                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String lat = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();


                        String lng = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();

                        LatLng orderloaction = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
                        bitmap = Common.scaleBitmap(bitmap, 70, 70);

                        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title("Order Of " + Common.currentRequest.getPhone())
                                .position(orderloaction);
                        mMap.addMarker(marker);

                        //Draw Route
                        mService.getDirections(yourLocation.latitude + "," + yourLocation.longitude,
                                orderloaction.latitude + "," + orderloaction.longitude,"AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY")
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        new ParserTask().execute(response.body().toString());

                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
        else
        {
            if (request.getLatLng() != null && !request.getLatLng().isEmpty())
            {
                String[] latLng=request.getLatLng().split(",");
                LatLng orderLocation=new LatLng(Double.parseDouble(latLng[0]),Double.parseDouble(latLng[1]));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
                bitmap = Common.scaleBitmap(bitmap, 70, 70);

                MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Order Of" + Common.currentRequest.getPhone())
                        .position(orderLocation);
                mMap.addMarker(marker);

              /*  mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?sensor=false&key=AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY&latlng=")
                        .append(request.getLatLng()).toString()) */

                        /* mLastLocation.getLatitude()+","+mLastLocation.getLongitude(),
                                                orderLocation.latitude+","+orderLocation.longitude */
                        mService.getDirections( mLastLocation.getLatitude()+","+mLastLocation.getLongitude(),
                                orderLocation.latitude+","+orderLocation.longitude,"AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY")
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                new ParserTask().execute(response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });

            }
        }
    }

    @Override
    protected void onStop() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        boolean isSuccess=mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_map_style));
        if (!isSuccess)
            Log.d("ERROR","Map Style Load Failed !!!");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                    mLastLocation = location;
                    LatLng yourLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mCurrentMarker = mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location !!!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
            }
        });
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        AlertDialog mDialog=new SpotsDialog.Builder().setContext(TrackingOrder.this).build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          //  mDialog.show();
            mDialog.setMessage("Please Wait . . .");

            try {
                mDialog.show();
            } catch (WindowManager.BadTokenException e) {
                Log.e("WindowManagerBad ", e.toString());
            }

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes=null;
            try {
                jsonObject=new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();
                routes=parser.parse(jsonObject);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
          //  mDialog.dismiss();

            try {
                mDialog.dismiss();
            } catch (WindowManager.BadTokenException e) {
                Log.e("WindowManagerBad ", e.toString());
            }

          //  ArrayList points=null;
            //PolylineOptions lineOptions=null;
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
            for (int i=0;i<lists.size();i++)
            {
                points=new ArrayList();
                lineOptions=new PolylineOptions();
                List<HashMap<String,String>> path=lists.get(i);

                for (int j=0;j<path.size();j++)
                {
                    HashMap<String,String>point=path.get(j);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));

                    LatLng position=new LatLng(lat,lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            mMap.addPolyline(lineOptions);
        }
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(TrackingOrder.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(TrackingOrder.this, REQUEST_LOCATION);

                                finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }
}

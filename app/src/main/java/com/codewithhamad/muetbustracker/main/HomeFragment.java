package com.codewithhamad.muetbustracker.main;

import static android.content.Context.ACTIVITY_SERVICE;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.googlemapclustering.MyClusterManagerRenderer;
import com.codewithhamad.muetbustracker.helper.HelperClass;
import com.codewithhamad.muetbustracker.databinding.FragmentHomeBinding;
import com.codewithhamad.muetbustracker.main.bottomsheet.BottomSheetFragment;
import com.codewithhamad.muetbustracker.main.bottomsheet.DirectionInterfaceCallBack;
import com.codewithhamad.muetbustracker.main.routesdirectionsapi.ReadTask;
import com.codewithhamad.muetbustracker.models.ClusterMarker;
import com.codewithhamad.muetbustracker.models.Point;
import com.codewithhamad.muetbustracker.models.PolylineData;
import com.codewithhamad.muetbustracker.models.Stop;
import com.codewithhamad.muetbustracker.models.User;
import com.codewithhamad.muetbustracker.models.UserLocation;
import com.codewithhamad.muetbustracker.services.LocationService;
import com.codewithhamad.muetbustracker.services.MarkerUpdateInterface;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends androidx.fragment.app.Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterItemClickListener<ClusterMarker>,
        DirectionInterfaceCallBack, RoutingListener,
        GoogleMap.OnMyLocationChangeListener{

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String androidId = "";
    private HelperClass helperClass;
    private MapView mapView;
    private boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 112233;
    private static final int ERROR_DIALOG_REQUEST = 110011;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 229922;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation currentUserLocation;
    private GoogleMap googleMap;
    private boolean[] isNearbyToastMessageShown= new boolean[6];

    // custom cluster marker rendering
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    // for point update
    private Handler mHandler= new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 5000;

//     direction api
    private GeoApiContext geoApiContext= null;
    private ArrayList<PolylineData> polylineData = new ArrayList<>();
    private Marker stopMarker= null;
    private ArrayList<Marker> stopMarkers= new ArrayList<>();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);


        // init views
        mapView = binding.googleMap;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        helperClass = new HelperClass(getContext());
        androidId = helperClass.getAndroidId();
        currentUserLocation= new UserLocation();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        initGoogleMap(savedInstanceState);

        // showing directions from PointAdapter call
        if(getArguments() != null){
            if(getArguments().getString("key").equals("calculateDirections")){
                String currentPointJson= getArguments().getString("currentPoint");
                if(currentPointJson != null){
                    Point currentPoint= null;
                    Gson gson = new Gson();
                    Type type = new TypeToken<Point>() {}.getType();
                    currentPoint = gson.fromJson(currentPointJson, type);

                    getLastKnownLocation();
                    final Point finalCurrentPoint1= currentPoint;

                    try {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (finalCurrentPoint1 != null)
                                    calculateDirections(finalCurrentPoint1);
                                else
                                    Toast.makeText(getContext(), "current point is null", Toast.LENGTH_SHORT).show();
                            }
                        }, 4000);
                    }
                    catch (Exception e){
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }


        return binding.getRoot();

    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(getContext(), LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                getActivity().startForegroundService(serviceIntent);
            }else{
                getActivity().startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) requireContext().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codewithhamad.muetbustracker.services".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    public void addMapMarkers(Context context) {

        Toast.makeText(context, "called", Toast.LENGTH_SHORT).show();
        if (googleMap != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity(), googleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        googleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            ArrayList<Point> all = helperClass.getFavPointFromSharedPref("all");
            for (Point p : all) {

                try {
                    if(p.getGeoPoint() == null || !p.isAvailable())
                        continue;

                    String snippet = "";
                    snippet = "This is you";

                    int avatar = R.drawable.point_logo_clickable; // set the default avatar

                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(p.getGeoPoint().getLatitude(), p.getGeoPoint().getLongitude()),
                            "p_num: " + p.getPointNumber(),
                            "",
                            avatar,
                            p
                    );

                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                }
                catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.setOnClusterItemClickListener(this);

            mClusterManager.cluster();
//            mClusterManager.setAnimation(true);

        }
    }

    private void setUserPosition() {

        // Set a boundary to start
//        double bottomBoundary = currentUserLocation.getGeoPoint().getLatitude() - .1;
//        double leftBoundary = currentUserLocation.getGeoPoint().getLongitude() - .1;
//        double topBoundary = currentUserLocation.getGeoPoint().getLatitude() + .1;
//        double rightBoundary = currentUserLocation.getGeoPoint().getLongitude() + .1;
//
//        mMapBoundary = new LatLngBounds(
//                new LatLng(bottomBoundary, leftBoundary),
//                new LatLng(topBoundary, rightBoundary)
//        );

//        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 10));
        if(currentUserLocation== null || googleMap ==null)
            return;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude()), 10));
    }


    private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    if (location != null) {
                        if (currentUserLocation == null)
                            currentUserLocation = new UserLocation();

                        currentUserLocation.setLatitude(location.getLatitude());
                        currentUserLocation.setLongitude(location.getLongitude());
                        currentUserLocation.setTimestamp(null);
                        updateUserLocation();
                        setUserPosition();
                        startLocationService();
//                    Log.d(TAG, "onComplete: " + currentUserLocation.getTimestamp());
                    }
                }
            }
        });

    }

    private void updateUserLocation(){
        if(currentUserLocation != null){
            database.getReference().child("Users").child(androidId).child("UserLocation").setValue(currentUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        Toast.makeText(getContext(), "success", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), "failure", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

        // initializing geoApiContext var
        if(geoApiContext == null){
            geoApiContext= new GeoApiContext.Builder()
                    .apiKey("AIzaSyDd-YBc_xqdQQEDZi-6Z1LgPDU3eFbhu6M").build();
        }
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK() {
        Log.d("check", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d("check", "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d("check", "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available,
                    ERROR_DIALOG_REQUEST);
            assert dialog != null;
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Toast.makeText(getContext(), "onRequest called", Toast.LENGTH_SHORT).show();
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLastKnownLocation();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("check", "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getLastKnownLocation();
//                    Toast.makeText(getContext(), "granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "dedied", Toast.LENGTH_SHORT).show();
                    getLocationPermission();
                }
            }
        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLastKnownLocation();
            Toast.makeText(getContext(), "granted", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        startUserLocationsRunnable();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getLastKnownLocation();
                startLocationService();
                Toast.makeText(getContext(), "granted", Toast.LENGTH_SHORT).show();
            } else {
                getLocationPermission();
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        // checking for location permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationChangeListener(this);
        this.googleMap= googleMap;
        addMapMarkers(getContext());


//        setCameraView();

    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }




    @Override
    public boolean onClusterItemClick(ClusterMarker item) {
        if(item.getPoint() != null) {

            // showing bottom sheet
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment("HomeFragment");
            bottomSheetFragment.setData(item.getPoint(), this);
            bottomSheetFragment.show(((AppCompatActivity) requireContext()).getSupportFragmentManager(),
                    bottomSheetFragment.getTag());
        }
        return true;
    }


    public void calculateDirections(Point currentPoint){
        Log.d(TAG, "calculateDirections: calculating directions.");

        try {
            if(currentUserLocation.getLatitude() == null)
                getLastKnownLocation();

            String url = getMapsApiDirectionsUrl(currentPoint);
            ReadTask downloadTask = new ReadTask(googleMap);
            downloadTask.execute(url);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentPoint.getGeoPoint().getLatitude(),
                            currentPoint.getGeoPoint().getLongitude()),
                    20));
            showStopMarkers(currentPoint.getStops());
        }
        catch (Exception e){
            Log.d(TAG, "calculateDirections: " + e.getLocalizedMessage());
            calculateDirections(currentPoint);
        }

        Toast.makeText(getContext(), "end of method", Toast.LENGTH_SHORT).show();
    }

    private String getMapsApiDirectionsUrl(Point currentPoint) {
        ArrayList<Stop> allStops= currentPoint.getStops();

        LatLng from= allStops.get(0).getStopCoordinates();
        LatLng to= allStops.get(allStops.size()-1).getStopCoordinates();

        String origin= "origin=" + from.latitude + "," + from.longitude;
        String destination = "destination=" + to.latitude +
                "," + to.longitude;

        String waypoints = "";
        for(int i=1; i < allStops.size()-1; i++){
            LatLng point  = (LatLng) allStops.get(i).getStopCoordinates();
            if(i==1)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        String sensor = "sensor=false";
        String params = origin+"&"+destination+"&"+sensor+"&"+waypoints;
        String output = "json";

        String url= "https://maps.googleapis.com/maps/api/directions/"+output+"?"
                +params+"&key=AIzaSyDd-YBc_xqdQQEDZi-6Z1LgPDU3eFbhu6M";

        return url;
    }

    // adding polyLines/showing routes
//    private void addPolyLinesToMap(final DirectionsResult result, Point currentPoint, com.google.maps.model.LatLng destination){
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "run: result routes: " + result.routes.length);
//
//
//                boolean isAdded= false;
//                ArrayList<LatLng> muetStops= getMuetStopsList();
//
//                // removing old polylines
//                if(polylineData.size() > 0){
//                    for(PolylineData p : polylineData)
//                        p.getPolyline().remove();
//                }
//                polylineData.clear();
//                polylineData= new ArrayList<>();
//
//                for(DirectionsRoute route: result.routes){
//                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
//                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
//
//                    List<LatLng> newDecodedPath = new ArrayList<>();
//
//                    // This loops through all the LatLng coordinates of ONE polyline.
//                    for(com.google.maps.model.LatLng latLng: decodedPath){
//
////                        Log.d(TAG, "run: latlng: " + latLng.toString());
//
//                        newDecodedPath.add(new LatLng(
//                                latLng.lat,
//                                latLng.lng
//                        ));
//                    }
//
//                    if (currentPoint.getPointNumber().equals("22") &&
//                            PolyUtil.isLocationOnPath(muetStops.get(6), newDecodedPath, false, 10)){
//                        isAdded= true;
//                        Toast.makeText(getContext(), "waypoint is included", Toast.LENGTH_SHORT).show();
//
//                        // "point" laying on source to destination path
//                        Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
//                        polyline.setColor(ContextCompat.getColor(getActivity(), R.color.blue));
//                        polyline.setClickable(false);
//                        polylineData.add(new PolylineData(polyline, route.legs[0]));
//
////                         showing stop marker
//                        showStopMarkers(muetStops);
////                        stopMarker= googleMap.addMarker(new MarkerOptions()
////                            .position(wayPoint));
////                        stopMarker.showInfoWindow();
//                    }
//                    else if(!isAdded){
//                        Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
//                        polyline.setColor(ContextCompat.getColor(getActivity(), R.color.blue));
//                        polyline.setClickable(false);
//                        polylineData.add(new PolylineData(polyline, route.legs[0]));
//
//                        // hiding stop marker
//                        hideStopMarkers();
////                        if(stopMarker != null && stopMarker.isVisible())
////                            stopMarker.setVisible(false);
//                    }
//                }
//            }
//        });
//    }

    private ArrayList<LatLng> getMuetStopsList() {
        ArrayList<LatLng> muetStops= new ArrayList<>();

        LatLng muetWorkShop= new LatLng(25.415639, 68.258339);
        LatLng cc= new LatLng(25.414418, 68.258426);
        LatLng stc= new LatLng(25.410348, 68.258841);
        LatLng zeroPoint= new LatLng(25.408489, 68.260428);
        LatLng elCs= new LatLng(25.407558, 68.260524);
        LatLng hilTop= new LatLng(25.407558, 68.260524);
        LatLng civilDept= new LatLng(25.401366, 68.257313);
        LatLng bioMedDept= new LatLng(25.40503, 68.259847);
        LatLng softDept= new LatLng(25.40545, 68.260808);
        LatLng muetMainGate= new LatLng(25.40813, 68.264737);

        muetStops.add(0, muetWorkShop);
        muetStops.add(1, cc);
        muetStops.add(2, stc);
        muetStops.add(3, zeroPoint);
        muetStops.add(4, muetMainGate);
        muetStops.add(5, softDept);
        muetStops.add(6, bioMedDept);
        muetStops.add(7, civilDept);
        muetStops.add(8, hilTop);
        muetStops.add(9, elCs);
        muetStops.add(10, muetWorkShop);

        return muetStops;
    }

    private void hideStopMarkers() {
        if(stopMarkers.size() > 0){
            Toast.makeText(getContext(), "hide stop merkers", Toast.LENGTH_SHORT).show();
            for(Marker marker : stopMarkers){
                if(marker != null && marker.isVisible())
                    marker.setVisible(false);
            }
        }
        stopMarkers.clear();
        stopMarkers= new ArrayList<>();
    }

    private void showStopMarkers(ArrayList<Stop> muetStops) {
        stopMarkers= new ArrayList<>();

        for(int i=1; i<muetStops.size()-2; i++){
            if(googleMap != null){
                String title= "";
                String snippet= "";

                float distanceInMeters= getDistanceInMeters(muetStops.get(0).getStopCoordinates(),
                        muetStops.get(i).getStopCoordinates());
                float speedInMetersPerMinute= getSpeedInMetersPerMinute(muetStops.get(0).getStopCoordinates());

                title= "Stop : " + muetStops.get(i).getStopStr()+". Approx. distance: " + (int)distanceInMeters + " meter(s)";

                if(speedInMetersPerMinute != 0 ) {
                    float estimatedTimeInMinutes = distanceInMeters / speedInMetersPerMinute;
                    snippet= "Est. time: " + estimatedTimeInMinutes + " minute(s).";
                }
                else{
                    snippet= "Est. time:(NIL) - Point is not moving";
                }

                Marker marker= googleMap.addMarker(new MarkerOptions()
                        .position(muetStops.get(i).getStopCoordinates())
                        .title(title)
                        .snippet(snippet));
//                marker.showInfoWindow();
                stopMarkers.add(marker);
            }
        }

    }

    private float getSpeedInMetersPerMinute(LatLng latLng) {
        Location currentPoint= new Location("");
        currentPoint.setLatitude(latLng.latitude);
        currentPoint.setLongitude(latLng.longitude);
        return currentPoint.getSpeed();
    }

    private float getDistanceInMeters(LatLng from, LatLng to) {
        Location location1= new Location("");
        location1.setLatitude(from.latitude);
        location1.setLongitude(from.longitude);

        Location location2= new Location("");
        location2.setLatitude(to.latitude);
        location2.setLongitude(to.longitude);
        return location1.distanceTo(location2);
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.d("check", e.getMessage());

    }

    @Override
    public void onRoutingStart() {
        Log.d("check", "onRoutingStart");

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        Log.d("check", "onRoutingSuccess");

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        List<Polyline> polylines = new ArrayList<>();

        googleMap.moveCamera(center);


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(R.color.red));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }
    }

    @Override
    public void onRoutingCancelled() {
        Log.d("check", "onRoutingCancelled");
    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: called.");
        try {

            String androidId= "8bfb3457d1c5782d";

            database.getReference().child("Users").child(androidId).child("UserLocation").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserLocation updatedUserLocation= null;
                    if(snapshot.exists())
                        updatedUserLocation = snapshot.getValue(UserLocation.class);

                    // update the location
                    if(updatedUserLocation==null)
                        return;
                    for (int i = 0; i < mClusterMarkers.size(); i++) {
                        try {
                            if (mClusterMarkers.get(i).getPoint().getPointNumber().equals("21")) {

                                LatLng updatedLatLng = new LatLng(
                                        updatedUserLocation.getLatitude(),
                                        updatedUserLocation.getLongitude()
                                );

                                mClusterMarkers.get(i).getPoint().setGeoPoint(
                                        new GeoPoint(updatedLatLng.latitude, updatedLatLng.longitude)
                                );
                                mClusterMarkers.get(i).setPosition(updatedLatLng);
                                mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                            }

                        }
                        catch (NullPointerException e) {
                            Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMyLocationChange(@NonNull Location location) {
        Log.d(TAG, "onMyLocationChange: called");
        Location targetMarker= new Location("target");

        String pointNumber= "";
        int pointIndex= -1;

        if(mClusterMarkers.size() > 0){
            for(int i=0; i<mClusterMarkers.size(); i++){
                Point point= mClusterMarkers.get(i).getPoint();
                if(point != null && point.isFavorite() && point.isAvailable())
                    if(point.getGeoPoint() != null) {
                        pointNumber= point.getPointNumber();
                        pointIndex= point.getPointIndex();
                        targetMarker.setLatitude(mClusterMarkers.get(i).getPoint().getGeoPoint().getLatitude());
                        targetMarker.setLongitude(mClusterMarkers.get(i).getPoint().getGeoPoint().getLongitude());

                        if(location.distanceTo(targetMarker) < 100 && !isNearbyToastMessageShown[pointIndex-1]){
                            isNearbyToastMessageShown[pointIndex-1]= true;
                            Toast.makeText(getContext(), "Point " + pointNumber + " is nearby your location", Toast.LENGTH_SHORT).show();
                            // TODO: 11/8/2021 send notification to user
                        }
                    }
            }
        }

    }

}
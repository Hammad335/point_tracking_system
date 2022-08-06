package com.codewithhamad.muetbustracker.helper;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.codewithhamad.muetbustracker.models.Point;
import com.codewithhamad.muetbustracker.models.Stop;
import com.codewithhamad.muetbustracker.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class HelperClass {

    private Context context;
    public static Double latitude= 25.3772;
    public static Double longitude=  68.3362;

    public HelperClass(Context context) {
        this.context= context;
    }

    public String getAndroidId() {
        return android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void getCurrentUser(FirebaseDatabase database, String androidId, FirebaseDatabaseCallBack callBack) {
        database.getReference().child("Users").child(androidId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                            callBack.currentUserCallBack(snapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean isOnline(){
        boolean isOnline = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                @SuppressLint("MissingPermission") NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
                isOnline = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
                isOnline = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOnline;
    }


    public ArrayList<Point> getFavPointFromSharedPref(String allOrfav){
        ArrayList<Point> allPoints = new ArrayList<>();

        ArrayList<Stop> allStops= getAllStops();

        allPoints.add(new Point(1,"Ahmed", "12", "2XY-3ED",
                "01:00 pm", true, false, new GeoPoint(27.7244, 68.8228),
                null));

        allPoints.add(new Point(2, "Ali", "22", "2X5-3DT",
                "01:00 pm", true, false, new GeoPoint(25.415658, 68.258318),
                allStops));

        allPoints.add(new Point(3, "Waiz", "21", "2WY-3VA",
                "01:00 pm", true, false, new GeoPoint(latitude, longitude),
                null));

        allPoints.add(new Point(4, "Hamad", "09", "4XD-3AB",
                "01:00 pm", false, false, null, null));

        allPoints.add(new Point(5, "Waiz", "24", "2WY-3VA",
                "01:00 pm", false, false, null, null));

        allPoints.add(new Point(6, "Hamad", "03", "4XD-3AB",
                "01:00 pm", false, false, null, null));


        // getting favorite points from sharedPref
        ArrayList<Point> fav= new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPref1", MODE_PRIVATE);
        if (sharedPreferences != null) {
            for (int i = 0; i < allPoints.size(); i++) {
                boolean isFav = sharedPreferences.getBoolean(allPoints.get(i).getPointNumber(), false);
                if (isFav) {
                    allPoints.get(i).setFavorite(isFav);
                    fav.add(0, allPoints.get(i));
                }
            }
        }
        ArrayList<Point> tempAllPoints= new ArrayList<>();
        if(allOrfav.equals("all")){
            for(int i=0; i<allPoints.size(); i++) {
                if (allPoints.get(i).isAvailable())
                    tempAllPoints.add(0, allPoints.get(i));
                else
                    tempAllPoints.add(tempAllPoints.size(), allPoints.get(i));
            }
        }
        return allOrfav.equals("all") ? tempAllPoints : fav;
    }

    private ArrayList<Stop> getAllStops() {
        ArrayList<Stop> tempAllStops= new ArrayList<>();

        tempAllStops.add(0, new Stop("workShop", new LatLng(25.415639, 68.258339)));
        tempAllStops.add(1, new Stop("cc", new LatLng(25.414418, 68.258426)));
        tempAllStops.add(2, new Stop("stc", new LatLng(25.410348, 68.258841)));
        tempAllStops.add(3, new Stop("zeroPoint", new LatLng(25.408489, 68.260428)));
        tempAllStops.add(4, new Stop("mainGate", new LatLng(25.40813, 68.264737)));
        tempAllStops.add(5, new Stop("softDept", new LatLng(25.40545, 68.260808)));
        tempAllStops.add(6, new Stop("bioDept", new LatLng(25.40503, 68.259847)));
        tempAllStops.add(7, new Stop("civilDept", new LatLng(25.401366, 68.257313)));
        tempAllStops.add(8, new Stop("hilTop", new LatLng(25.407558, 68.260524)));
        tempAllStops.add(9, new Stop("elCs", new LatLng(25.407558, 68.260524)));
        tempAllStops.add(10, new Stop("workShop", new LatLng(25.415639, 68.258339)));
        tempAllStops.add(11, new Stop("mainGate", new LatLng(25.40813, 68.264737)));

        return tempAllStops;
    }

}

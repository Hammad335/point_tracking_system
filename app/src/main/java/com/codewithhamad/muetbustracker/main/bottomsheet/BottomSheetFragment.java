package com.codewithhamad.muetbustracker.main.bottomsheet;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.databinding.FragmentBottomSheetBinding;
import com.codewithhamad.muetbustracker.main.HomeFragment;
import com.codewithhamad.muetbustracker.main.MainActivity;
import com.codewithhamad.muetbustracker.models.Point;
import com.codewithhamad.muetbustracker.models.Stop;
import com.codewithhamad.muetbustracker.models.UserLocation;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.core.ActivityScope;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class BottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetBinding binding;
    private Point currentPoint;
    private DirectionInterfaceCallBack directionInterfaceCallBack= null;
    private Marker marker= null;
    private String callingContext= null;

    public BottomSheetFragment(String callingContext){
        this.callingContext= callingContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);

        // setting views
        if(currentPoint != null)
            setViews();


        return binding.getRoot();
    }


    public void setData(Point currentPoint, Context context ) {
        this.currentPoint= currentPoint;
    }

    public void setData(Point currentPoint, DirectionInterfaceCallBack directionInterfaceCallBack) {
        this.currentPoint= currentPoint;
        this.directionInterfaceCallBack= directionInterfaceCallBack;
        this.marker= marker;
    }


    private void setViews() {
        binding.bottomSheetHeadingText.setText("Point " + currentPoint.getPointNumber() + " Route");
        binding.pointNumber.setText(currentPoint.getPointNumber() + "");
        binding.driverName.setText(currentPoint.getDriverName() + "");
        binding.pointTiming.setText(currentPoint.getLeavingTime());

        // setting points stops
        if(currentPoint.getStops() != null) {
            ArrayList<Stop> allStops= currentPoint.getStops();

            binding.pointFrom.setText(allStops.get(0).getStopStr());

            // setting wayPoints stops
            try {
                ArrayList<LinearLayout> stopTextViewLayouts= getAllStopTextViewLayouts();
                ArrayList<TextView> stopTextViews = getAllStopTextViews();
                setWayPointsTextViews(allStops, stopTextViews, stopTextViewLayouts);
            }
            catch (Exception e){
                Log.d("bottomSheet", "setViews: " + e.getLocalizedMessage());
            }

            binding.pointFinalStop.setText(allStops.get(allStops.size()-1).getStopStr());
        }

        if (currentPoint.isAvailable()) {
            binding.showTrackRoute.setEnabled(true);
            binding.showTrackRoute.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        else {
            binding.showTrackRoute.setEnabled(false);
            binding.showTrackRoute.setBackgroundColor(getResources().getColor(R.color.grey));
        }

        binding.showTrackRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPoint.isAvailable()){
                    // navigate user to map activity

                    if(currentPoint.getStops() == null){
                        Toast.makeText(getContext(), "No route available", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    if(callingContext.equals("PointAdapter")){
                        dismiss();
                        Gson gson = new Gson();
                        String currentPointJson = gson.toJson(currentPoint);

                        Bundle args = new Bundle();
                        args.putString("key", "calculateDirections");
                        args.putString("currentPoint", currentPointJson);

                        Fragment fragment= new HomeFragment();
                        fragment.setArguments(args);
                        loadFragment(fragment);
                        MainActivity.navigationView.setCheckedItem(R.id.home);
                        return;
                    }

                    directionInterfaceCallBack.calculateDirections(currentPoint);
                    dismiss();
                }
            }
        });

    }

    private ArrayList<LinearLayout> getAllStopTextViewLayouts() {
        ArrayList<LinearLayout> stopTextViewLayouts= new ArrayList<>();

        stopTextViewLayouts.add(0, null);
        stopTextViewLayouts.add(1, binding.pointFirstStopLayout);
        stopTextViewLayouts.add(2, binding.pointSecondStopLayout);
        stopTextViewLayouts.add(3, binding.pointThirdStopLayout);
        stopTextViewLayouts.add(4, binding.pointFourthStopLayout);
        stopTextViewLayouts.add(5, binding.pointFifthStopLayout);
        stopTextViewLayouts.add(6, binding.pointSixthStopLayout);
        stopTextViewLayouts.add(7, binding.pointSeventhStopLayout);
        stopTextViewLayouts.add(8, binding.pointEigthStopLayout);
        stopTextViewLayouts.add(9, binding.pointNinthStopLayout);
        stopTextViewLayouts.add(10, binding.pointTenthStopLayout);

        return stopTextViewLayouts;
    }

    private ArrayList<TextView> getAllStopTextViews() {
        ArrayList<TextView> allStops= new ArrayList<>();
        allStops.add(0, null);
        allStops.add(1, binding.pointStop1);
        allStops.add(2, binding.pointStop2);
        allStops.add(3, binding.pointStop3);
        allStops.add(4, binding.pointStop4);
        allStops.add(5, binding.pointStop5);
        allStops.add(6, binding.pointStop6);
        allStops.add(7, binding.pointStop7);
        allStops.add(8, binding.pointStop8);
        allStops.add(9, binding.pointStop9);
        allStops.add(10, binding.pointStop10);
        return allStops;
    }

    private void setWayPointsTextViews(ArrayList<Stop> stops, ArrayList<TextView> stopTextViews,
                                       ArrayList<LinearLayout> stopTextViewLayouts) {

        for(int i=1; i<stops.size()-1; i++){
            stopTextViews.get(i).setText(stops.get(i).getStopStr());
            stopTextViewLayouts.get(i).setVisibility(View.VISIBLE);
        }
    }

    private void setStops(String pointStopStr, TextView pointStopTextView, LinearLayout stopLayout) {
        if(pointStopStr == null)
            stopLayout.setVisibility(View.GONE);
        else{
            pointStopTextView.setText(pointStopStr);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager= getParentFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment).commit();
        MainActivity.drawerLayout.closeDrawer(GravityCompat.START);
        fragmentTransaction.addToBackStack(null);
    }

}
package com.codewithhamad.muetbustracker.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codewithhamad.muetbustracker.helper.HelperClass;
import com.codewithhamad.muetbustracker.adapters.PointAdapter;
import com.codewithhamad.muetbustracker.databinding.FragmentPointBinding;
import com.codewithhamad.muetbustracker.models.Point;

import java.util.ArrayList;


public class SelectPointFragment extends androidx.fragment.app.Fragment {


    private FragmentPointBinding binding;
    private PointAdapter pointAdapter;
    public static ArrayList<Point> allPoints;
    private HelperClass helperClass;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPointBinding.inflate(inflater, container, false);

        // init allPoints
        helperClass= new HelperClass(getContext());
        allPoints= helperClass.getFavPointFromSharedPref("all");

        if(allPoints.size() != 0) {
            pointAdapter = new PointAdapter(getContext(), allPoints, "SelectPointFragment");
            binding.selectPointFragRecView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.selectPointFragRecView.setAdapter(pointAdapter);
        }


        return binding.getRoot();
    }

}
package com.codewithhamad.muetbustracker.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codewithhamad.muetbustracker.helper.HelperClass;
import com.codewithhamad.muetbustracker.adapters.PointAdapter;
import com.codewithhamad.muetbustracker.databinding.FragmentMyPointBinding;
import com.codewithhamad.muetbustracker.models.Point;

import java.util.ArrayList;

public class MyPointFragment extends Fragment {

    private FragmentMyPointBinding binding;
    public static ArrayList<Point> favoritePoints;
    private PointAdapter pointAdapter;
    private HelperClass helperClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyPointBinding.inflate(inflater, container, false);

        // init views
        helperClass= new HelperClass(getContext());
        favoritePoints= new ArrayList<>();
        favoritePoints= helperClass.getFavPointFromSharedPref("fav");

        if(favoritePoints.size() != 0) {
            pointAdapter = new PointAdapter(getContext(), favoritePoints, "MyPointFragment");
            binding.myPointFragRecView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.myPointFragRecView.setAdapter(pointAdapter);
        }



        return binding.getRoot();
    }



}
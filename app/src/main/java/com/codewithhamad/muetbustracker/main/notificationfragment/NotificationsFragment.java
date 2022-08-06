package com.codewithhamad.muetbustracker.main.notificationfragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codewithhamad.muetbustracker.adapters.NotificationsAdapter;
import com.codewithhamad.muetbustracker.databinding.FragmentNotificationsBinding;
import com.codewithhamad.muetbustracker.models.Notification;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment implements NotificationCallBackInterface {

    private FragmentNotificationsBinding binding;
    public static ArrayList<Notification> allNotifications;
    private NotificationsAdapter notificationsAdapter;
    private int unreadCount= 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        // init views
        allNotifications= new ArrayList<>();
        allNotifications= getNotificationsFromSharePref();

        if(allNotifications.size() != 0) {
            notificationsAdapter = new NotificationsAdapter(getContext(), allNotifications, this);
            binding.notificationFragRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.notificationFragRecyclerView.setAdapter(notificationsAdapter);
        }




        return binding.getRoot();
    }

    private ArrayList<Notification> getNotificationsFromSharePref() {
        ArrayList<Notification> temp= new ArrayList<>();
        temp.add(new Notification("Notification 1", "Point Arrived", false));
        temp.add(new Notification("Notification 2", "Point Arrived", false));
        temp.add(new Notification("Notification 3", "Point Arrived", false));
        temp.add(new Notification("Notification 4", "Point Arrived", false));
        temp.add(new Notification("Notification 5", "Point Arrived", false));
        temp.add(new Notification("Notification 6", "Point Arrived", false));
        temp.add(new Notification("Notification 7", "Point Arrived", false));
        temp.add(new Notification("Notification 8", "Point Arrived", false));
        temp.add(new Notification("Notification 9", "Point Arrived", false));
        temp.add(new Notification("Notification 10", "Point Arrived", false));
        temp.add(new Notification("Notification 11", "Point Arrived", false));
        temp.add(new Notification("Notification 12", "Point Arrived", false));
        temp.add(new Notification("Notification 13", "Point Arrived", false));
        temp.add(new Notification("Notification 14", "Point Arrived", false));
        temp.add(new Notification("Notification 15", "Point Arrived", false));

        // getting favorite from sharedPref
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("unreadNotifications", MODE_PRIVATE);
        if (sharedPreferences != null) {
            for (int i = 0; i < temp.size(); i++) {
                boolean isRead = sharedPreferences.getBoolean(i+"", false);
                if (isRead)
                    temp.get(i).setRead(isRead);
                else
                    unreadCount++;
            }
            // setting unread notifications text
            binding.unreadNotificationsText.setText("Unread (" + unreadCount + ")");
        }
        return temp;
    }

    @Override
    public void setCallBack() {
        if(unreadCount!=0) {
            unreadCount--;
            binding.unreadNotificationsText.setText("Unread (" + unreadCount + ")");
        }
    }
}
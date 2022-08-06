package com.codewithhamad.muetbustracker.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.main.notificationfragment.NotificationCallBackInterface;
import com.codewithhamad.muetbustracker.models.Notification;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder>{

    Context context;
    ArrayList<Notification> allNotifications;
    NotificationCallBackInterface notificationCallBackInterface;

    public NotificationsAdapter(Context context, ArrayList<Notification> allNotifications, NotificationCallBackInterface notificationCallBackInterface) {
        this.context = context;
        this.allNotifications = allNotifications;
        this.notificationCallBackInterface = notificationCallBackInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.sample_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Notification currentNotification= allNotifications.get(position);
        if(currentNotification != null){
            holder.notificationsText.setText(currentNotification.getNotificationText());
            holder.notificationsSubText.setText(currentNotification.getNotificationSubText());
            holder.notificationIcon.setImageResource((currentNotification.isRead())?R.drawable.notification_icon:R.drawable.unred_notification_icon);

            // handling on click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(currentNotification.isRead())) {
                        currentNotification.setRead(true);
                        holder.notificationIcon.setImageResource(R.drawable.notification_icon);
                        notificationCallBackInterface.setCallBack();
                        saveToSharedPref(position+"", currentNotification.isRead());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return  allNotifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView notificationIcon;
        TextView notificationsText, notificationsSubText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationIcon= itemView.findViewById(R.id.notification_icon_imageview);
            notificationsText= itemView.findViewById(R.id.notification_text);
            notificationsSubText= itemView.findViewById(R.id.notifications_sub_text);
        }
    }

    private void saveToSharedPref(String position, boolean read) {
        SharedPreferences sharedPreferences= context.getSharedPreferences("unreadNotifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(position, read);
        editor.apply();
    }
}

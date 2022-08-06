package com.codewithhamad.muetbustracker.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.main.bottomsheet.BottomSheetFragment;
import com.codewithhamad.muetbustracker.models.Point;

import java.util.ArrayList;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Point> allPoints;
    private String callingFrag="";

    public PointAdapter(Context context, ArrayList<Point> allPoints, String callingFrag) {
        this.context = context;
        this.allPoints = allPoints;
        this.callingFrag= callingFrag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.sample_point, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Point currentPoint= allPoints.get(position);

        if(currentPoint == null)
            return;

        holder.driverName.setText(currentPoint.getDriverName());
        holder.pointNumber.setText(currentPoint.getPointNumber());
        holder.isActiveImageView.setImageResource(currentPoint.isAvailable()?R.drawable.status_active:R.drawable.status_inactive);
        holder.favoriteImageView.setImageResource(currentPoint.isFavorite()?R.drawable.favorite_icon:R.drawable.unfavorite_icon);

        holder.favoriteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPoint.isFavorite()){
                    currentPoint.setFavorite(false);
                    holder.favoriteImageView.setImageResource(R.drawable.unfavorite_icon);
                    if(callingFrag.equals("MyPointFragment")){
                        try {
                            // removing items from MyPointFrag
                            if (removeItem(position)) {
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount());
                            }
                        }
                        catch (Exception e){
                            Toast.makeText(context, position + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                    saveToSharedPref(currentPoint.getPointNumber(), currentPoint.isFavorite());
                }
                else{
                    currentPoint.setFavorite(true);
                    holder.favoriteImageView.setImageResource(R.drawable.favorite_icon);
                    saveToSharedPref(currentPoint.getPointNumber(), currentPoint.isFavorite());
                }
            }
        });

        // on clicking point item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // showing bottom sheet
                BottomSheetFragment bottomSheetFragment= new BottomSheetFragment("PointAdapter");
                bottomSheetFragment.setData(currentPoint, context);
                bottomSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                        bottomSheetFragment.getTag());
            }
        });

    }

    @Override
    public int getItemCount() {
        return allPoints.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pointNumber, driverName;
        ImageView favoriteImageView, isActiveImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pointNumber= itemView.findViewById(R.id.pointNumber);
            driverName= itemView.findViewById(R.id.driverName);
            favoriteImageView = itemView.findViewById(R.id.favoriteImageView);
            isActiveImageView= itemView.findViewById(R.id.isActiveImageView);
        }
    }

    private void saveToSharedPref(String pointNumber, boolean favorite) {
        SharedPreferences sharedPreferences= context.getSharedPreferences("sharedPref1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(pointNumber, favorite);
        editor.apply();
    }

    private boolean removeItem(int position) {
        if (allPoints.size() >= position + 1) {
            allPoints.remove(position);
            return true;
        }
        return false;
    }

}

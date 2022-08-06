package com.codewithhamad.muetbustracker.main.routesdirectionsapi;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.codewithhamad.muetbustracker.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    GoogleMap googleMap= null;

    public ParserTask(GoogleMap googleMap){
        this.googleMap= googleMap;
    }

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);
            Log.d("parsetask", "doInBackground: " + routes.size());
        }
        catch (Exception e) {
            Log.d("parsetask", "doInBackground: " + e.getLocalizedMessage());
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
        ArrayList<LatLng> points = null;
        PolylineOptions polyLineOptions = null;


        // traversing through routes
        try {

            Log.d("parsetask", "onPostExecute: parse task " + routes.size());
            Log.d("parsetask", "onPostExecute: " + routes.toString());

            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(R.color.blue);
            }
            if(polyLineOptions == null)
                Log.d("parser", "onPostExecute: polyLineOptions is null");
            if(googleMap == null)
                Log.d("parser", "onPostExecute: googleMap is null");

            googleMap.addPolyline(polyLineOptions);
        }
        catch (Exception e){
            Log.d("parsetask", "onPostExecute: " + e.getLocalizedMessage());
        }
    }
}
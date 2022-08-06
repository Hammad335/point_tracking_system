package com.codewithhamad.muetbustracker.main.routesdirectionsapi;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;

public class ReadTask extends AsyncTask<String, Void, String> {

    private GoogleMap googleMap= null;

    public ReadTask(GoogleMap googleMap){
        this.googleMap= googleMap;
    }

    @Override
    protected String doInBackground(String... url) {
        String data = "";
        try {
            HttpConnection http = new HttpConnection();
            data = http.readUrl(url[0]);
        }
        catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        new ParserTask(googleMap).execute(result);
    }
}

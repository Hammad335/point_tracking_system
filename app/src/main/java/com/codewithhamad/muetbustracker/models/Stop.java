package com.codewithhamad.muetbustracker.models;

import com.google.android.gms.maps.model.LatLng;

public class Stop {
    String stopStr;
    LatLng stopCoordinates;

    public Stop(String stopStr, LatLng stopCoordinates) {
        this.stopStr = stopStr;
        this.stopCoordinates = stopCoordinates;
    }

    public String getStopStr() {
        return stopStr;
    }

    public void setStopStr(String stopStr) {
        this.stopStr = stopStr;
    }

    public LatLng getStopCoordinates() {
        return stopCoordinates;
    }

    public void setStopCoordinates(LatLng stopCoordinates) {
        this.stopCoordinates = stopCoordinates;
    }
}

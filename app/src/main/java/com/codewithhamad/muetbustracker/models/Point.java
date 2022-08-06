package com.codewithhamad.muetbustracker.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Point {
    int pointIndex;
    String driverName;
    String pointNumber;
    String licenseNumber;
    String leavingTime;
    boolean isAvailable;
    boolean isFavorite;
    GeoPoint geoPoint;
    ArrayList<Stop> stops;

    public Point(int pointIndex, String driverName, String pointNumber, String licenseNumber,
                 String leavingTime, boolean isAvailable, boolean isFavorite, GeoPoint geoPoint,
                 ArrayList<Stop> stops) {
        this.pointIndex = pointIndex;
        this.driverName = driverName;
        this.pointNumber = pointNumber;
        this.licenseNumber = licenseNumber;
        this.leavingTime = leavingTime;
        this.isAvailable = isAvailable;
        this.isFavorite = isFavorite;
        this.geoPoint = geoPoint;
        this.stops = stops;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getPointNumber() {
        return pointNumber;
    }

    public void setPointNumber(String pointNumber) {
        this.pointNumber = pointNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(String leavingTime) {
        this.leavingTime = leavingTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public int getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(int pointIndex) {
        this.pointIndex = pointIndex;
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public void setStops(ArrayList<Stop> stops) {
        this.stops = stops;
    }
}

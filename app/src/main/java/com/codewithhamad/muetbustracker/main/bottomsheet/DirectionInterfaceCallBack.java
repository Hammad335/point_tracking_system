package com.codewithhamad.muetbustracker.main.bottomsheet;

import com.codewithhamad.muetbustracker.models.Point;
import com.google.android.gms.maps.model.Marker;

public interface DirectionInterfaceCallBack {

    void calculateDirections(Point currentPoint);
}

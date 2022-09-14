package com.yh.parkingpartner.model;

import java.io.Serializable;

public class LatLngLiteral implements Serializable {
    private  double lat;
    private  double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}

package com.yh.parkingpartner.model;

import java.io.Serializable;

public class Geometry implements Serializable {
    private  LatLngLiteral location;

    public LatLngLiteral getLocation() {
        return location;
    }

    public void setLocation(LatLngLiteral location) {
        this.location = location;
    }
}

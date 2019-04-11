package com.flcat.postnote;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PositionItem implements ClusterItem {
    double lat;

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

    public void setTitle(String title) {
        this.title = title;
    }

    double lng;
    private LatLng mPosition;
    public String title;

    public PositionItem(double lat, double lng, String title) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        }

    @Override
    public LatLng getPosition() {
        mPosition = new LatLng(lat, lng);
        return mPosition;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}

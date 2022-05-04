package com.miscrew.aednow;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;

public class InfoWindowRefresher implements Callback {
    private Marker marker;
    private String url;

    public InfoWindowRefresher(Marker marker, String url) {
        this.marker = marker;
        this.url = url;
    }

    @Override
    public void onSuccess() {
        if (marker != null && marker.isInfoWindowShown()) {
            // image is loaded, so hide then show window to refresh view
            marker.hideInfoWindow();
            marker.showInfoWindow();
        }
    }

    @Override
    public void onError(Exception e) {

    }
}

package com.miscrew.aednow;

import java.util.ArrayList;

public class MapData {

    // marker ID
    private String marker;
    private String title;
    private String snippet;
    private ArrayList<String> images;
    private double lat;
    private double lng;
    private int votes;
    private int icon;

    public MapData(String title, String snippet, double lat, double lng, int icon, String markerId) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.icon = icon;
        this.snippet = snippet;
        this.marker = markerId;
    }

    public MapData(String title, String snippet, double lat, double lng, int icon) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.icon = icon;
        this.snippet = snippet;
        this.marker = null;
    }

    public MapData(String title, String snippet, double lat, double lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.icon = 0;
        this.snippet = snippet;
        this.marker = null;
    }

    public MapData() {

    }


    // setters and getters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) { this.snippet = snippet; }

    public double getLat() {
        return lat;
    }

    public void setLat(long lat) { this.lat = lat; }

    public double getLng() {
        return lng;
    }

    public void setLng(long lng) {
        this.lng = lng;
    }

    public int getIcon() {return icon; }

    public void setIcon(int icon) {this.icon = icon; }

    public int getVotes() { return votes; }

    public void setVotes(int votes) { this.votes = votes; }

    public String getMarker() { return this.marker; }

    public void setMarker(String marker) { this.marker = marker; }

}

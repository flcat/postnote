package com.flcat.postnote;

public class ListViewItem {

    private String email;
    private String title;
    private String content;
    private String mUri;
    private String mThumbUri;
    private String date;
    private String num;
    private String lat;
    private String lng;

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getmUri() {
        return mUri;
    }

    public void setmUri(String mUri) {
        this.mUri = mUri;
    }

    public String getmThumbUri() {
        return mThumbUri;
    }

    public void setmThumbUri(String mThumbUri) {
        this.mThumbUri = mThumbUri;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public ListViewItem(String email, String title, String content, String mUri, String mThumbUri, String date, String num, String lat, String lng) {
        this.email = email;
        this.title = title;
        this.content = content;
        this.mUri = mUri;
        this.mThumbUri = mThumbUri;
        this.date = date;
        this.num = num;
        this.lat = lat;
        this.lng = lng;
    }
}